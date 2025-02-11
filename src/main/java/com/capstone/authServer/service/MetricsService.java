package com.capstone.authServer.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket;
import co.elastic.clients.elasticsearch._types.aggregations.RangeAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.RangeBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.capstone.authServer.dto.ScanToolType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricsService {

    private final ElasticsearchClient esClient;

    public MetricsService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    /**
     * 1) Distribution of all findings by their toolType
     *    e.g. [ { "toolType":"CODE_SCAN", "count":12 }, ... ]
     */
    public List<Map<String, Object>> getToolDistribution() throws IOException {
        Aggregation toolAgg = Aggregation.of(a -> a
            .terms(t -> t.field("toolType.keyword").size(10))
        );

        SearchRequest sr = SearchRequest.of(s -> s
            .index("findings")
            .size(0)
            .aggregations("toolAgg", toolAgg)
        );

        SearchResponse<Void> resp = esClient.search(sr, Void.class);

        var bucketAgg = resp.aggregations().get("toolAgg").sterms();
        var toolBuckets = bucketAgg.buckets().array();

        List<Map<String, Object>> result = new ArrayList<>();
        for (StringTermsBucket bucket : toolBuckets) {
            // Use bucket.key().stringValue() to get the actual string
            String toolTypeStr = bucket.key().stringValue();
            long docCount = bucket.docCount();

            Map<String, Object> item = new HashMap<>();
            item.put("toolType", toolTypeStr);
            item.put("count", docCount);
            result.add(item);
        }

        return result;
    }

    /**
     * 2) Distribution of states for the given tool type
     *    e.g. [ { "state":"OPEN", "count":10 }, { "state":"DISMISSED", "count":5 } ... ]
     */
    public List<Map<String, Object>> getStateDistribution(ScanToolType toolType) throws IOException {
        Aggregation filterAgg = Aggregation.of(a -> a
            .filter(f -> f.term(t -> t
                .field("toolType.keyword")
                .value(toolType.name())
            ))
            .aggregations("stateAgg", Aggregation.of(sub -> sub
                .terms(t -> t.field("state.keyword").size(10))
            ))
        );

        SearchRequest sr = SearchRequest.of(s -> s
            .index("findings")
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
     * 3) Distribution of severities for the given tool type
     *    e.g. [ { "severity":"CRITICAL", "count":2 }, ... ]
     */
    public List<Map<String, Object>> getSeverityDistribution(ScanToolType toolType) throws IOException {
        Aggregation filterAgg = Aggregation.of(a -> a
            .filter(f -> f.term(t -> t
                .field("toolType.keyword")
                .value(toolType.name())
            ))
            .aggregations("severityAgg", Aggregation.of(sub -> sub
                .terms(t -> t.field("severity.keyword").size(10))
            ))
        );

        SearchRequest sr = SearchRequest.of(s -> s
            .index("findings")
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
     * Example: build a histogram aggregator for the numeric "cvss" field with interval 1.0
     */
    public List<Map<String, Object>> getCvssHistogram() throws IOException {
        /*
         * We'll do a histogram aggregator with a script that:
         * 1) Checks doc['cvss.keyword'].size() != 0
         * 2) Tries Double.parseDouble(...) 
         * 3) If missing/invalid => return -1
         */
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
        return -1.0; // fallback if parse fails
      }
    } else {
      return -1.0; // fallback if field missing
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
            .index("findings")
            .size(0)
            .aggregations("cvssHist", histAgg)
        );
    
        SearchResponse<Void> resp = esClient.search(sr, Void.class);
    
        var histogramAgg = resp.aggregations().get("cvssHist").histogram();
        var buckets = histogramAgg.buckets().array();
    
        List<Map<String, Object>> results = new ArrayList<>();
        for (HistogramBucket bucket : buckets) {
            double key = bucket.key();    // e.g. 0.0,1.0,2.0,...
            long docCount = bucket.docCount();
    
            // If key == -1 => those are docs we couldn't parse or missing. 
            // We can skip them or keep them. If skipping:
            if (key < 0) continue;
    
            Map<String, Object> item = new HashMap<>();
            item.put("bucket", key);
            item.put("count", docCount);
            results.add(item);
        }
        return results;
    }
    
}
