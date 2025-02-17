package com.capstone.authServer.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket;
import co.elastic.clients.elasticsearch._types.aggregations.RangeBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.capstone.authServer.enums.ToolTypes;
import com.capstone.authServer.model.Tenant;
import com.capstone.authServer.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class MetricsService {

    private final ElasticsearchClient esClient;
    private final TenantRepository tenantRepo;

    public MetricsService(ElasticsearchClient esClient, TenantRepository tenantRepo) {
        this.esClient = esClient;
        this.tenantRepo = tenantRepo;
    }

    /**
     * Example #1: Distribution of all findings by their toolType
     *    e.g. [ { "toolType":"CODE_SCAN", "count":12 }, ... ]
     */
    public List<Map<String, Object>> getToolDistribution(String tenantId) throws IOException {
        Tenant tenant = fetchTenant(tenantId);

        // Aggregation that groups by toolType.keyword
        Aggregation toolAgg = Aggregation.of(a -> a
            .terms(t -> t.field("toolType.keyword").size(10))
        );

        SearchRequest sr = SearchRequest.of(s -> s
            .index(tenant.getEsIndex())  // Use the tenant’s ES index
            .size(0)
            .aggregations("toolAgg", toolAgg)
        );

        SearchResponse<Void> resp = esClient.search(sr, Void.class);

        var bucketAgg = resp.aggregations().get("toolAgg").sterms();
        var toolBuckets = bucketAgg.buckets().array();

        List<Map<String, Object>> result = new ArrayList<>();
        for (StringTermsBucket bucket : toolBuckets) {
            String toolTypeStr = bucket.key().stringValue();  // e.g. "CODE_SCAN"
            long docCount = bucket.docCount();

            Map<String, Object> item = new HashMap<>();
            item.put("toolType", toolTypeStr);
            item.put("count", docCount);
            result.add(item);
        }

        return result;
    }

    /**
     * Example #2: Distribution of states for the given tool type
     *    e.g. [ { "state":"OPEN", "count":10 }, { "state":"DISMISSED", "count":5 } ... ]
     */
    public List<Map<String, Object>> getStateDistribution(String tenantId, ToolTypes toolType) throws IOException {
        Tenant tenant = fetchTenant(tenantId);

        // We filter by toolType.keyword == <toolType.name()>, then do a sub-agg by state.keyword
        Aggregation filterAgg = Aggregation.of(a -> a
            .filter(f -> f.term(t -> t.field("toolType.keyword").value(toolType.name())))
            .aggregations("stateAgg", Aggregation.of(sub -> sub
                .terms(t -> t.field("state.keyword").size(10))
            ))
        );

        SearchRequest sr = SearchRequest.of(s -> s
            .index(tenant.getEsIndex())
            .size(0)
            .aggregations("filtered", filterAgg)
        );

        SearchResponse<Void> resp = esClient.search(sr, Void.class);

        var filter = resp.aggregations().get("filtered").filter();
        var stateAgg = filter.aggregations().get("stateAgg").sterms();
        var stateBuckets = stateAgg.buckets().array();

        List<Map<String, Object>> results = new ArrayList<>();
        for (StringTermsBucket bucket : stateBuckets) {
            String stateStr = bucket.key().stringValue();
            long count = bucket.docCount();

            Map<String, Object> item = new HashMap<>();
            item.put("state", stateStr);
            item.put("count", count);
            results.add(item);
        }
        return results;
    }

    /**
     * Example #3: Distribution of severities for the given tool type
     *    e.g. [ { "severity":"CRITICAL", "count":2 }, ... ]
     */
    public List<Map<String, Object>> getSeverityDistribution(String tenantId, ToolTypes toolType) throws IOException {
        Tenant tenant = fetchTenant(tenantId);

        Aggregation filterAgg = Aggregation.of(a -> a
            .filter(f -> f.term(t -> t.field("toolType.keyword").value(toolType.name())))
            .aggregations("severityAgg", Aggregation.of(sub -> sub
                .terms(t -> t.field("severity.keyword").size(10))
            ))
        );

        SearchRequest sr = SearchRequest.of(s -> s
            .index(tenant.getEsIndex())
            .size(0)
            .aggregations("filtered", filterAgg)
        );

        SearchResponse<Void> resp = esClient.search(sr, Void.class);

        var filter = resp.aggregations().get("filtered").filter();
        var severityAgg = filter.aggregations().get("severityAgg").sterms();
        var sevBuckets = severityAgg.buckets().array();

        List<Map<String, Object>> results = new ArrayList<>();
        for (StringTermsBucket bucket : sevBuckets) {
            String sevStr = bucket.key().stringValue();
            long count = bucket.docCount();

            Map<String, Object> item = new HashMap<>();
            item.put("severity", sevStr);
            item.put("count", count);
            results.add(item);
        }
        return results;
    }

    /**
     * Example #4: build a histogram aggregator for numeric "cvss" field with interval=1.0
     * If the field is stored as a keyword, we might parse it via a script or 
     * (preferably) store it as a float/double type in ES.
     */
    public List<Map<String, Object>> getCvssHistogram(String tenantId) throws IOException {
        Tenant tenant = fetchTenant(tenantId);

        Aggregation histAgg = Aggregation.of(a -> a
            .histogram(h -> h
                .script(script -> script
                    .lang("painless")
                    .source("""
    if (doc['cvss.keyword'].size() != 0) {
      def str = doc['cvss.keyword'].value;
      try {
        double val = Double.parseDouble(str);
        return val;
      } catch (Exception e) {
        return -1.0;
      }
    } else {
      return -1.0;
    }
    """)
                )
                .interval(1.0)
                .extendedBounds(b -> b
                    .min(0.0)
                    .max(10.0)
                )
            )
        );

        SearchRequest sr = SearchRequest.of(s -> s
            .index(tenant.getEsIndex())
            .size(0)
            .aggregations("cvssHist", histAgg)
        );

        SearchResponse<Void> resp = esClient.search(sr, Void.class);

        var histogramAgg = resp.aggregations().get("cvssHist").histogram();
        var buckets = histogramAgg.buckets().array();

        List<Map<String, Object>> results = new ArrayList<>();
        for (HistogramBucket bucket : buckets) {
            double key = bucket.key();    // e.g. 0.0, 1.0, 2.0,...
            long docCount = bucket.docCount();

            // If key < 0 => we skip (meaning parse failure or missing field).
            if (key < 0) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("bucket", key);
            item.put("count", docCount);
            results.add(item);
        }
        return results;
    }

    // Utility method: fetch tenant by tenantId
    private Tenant fetchTenant(String tenantId) {
        Tenant tenant = tenantRepo.findByTenantId(tenantId);
        if (tenant == null) {
            throw new RuntimeException("No Tenant found for tenantId: " + tenantId);
        }
        return tenant;
    }
}
