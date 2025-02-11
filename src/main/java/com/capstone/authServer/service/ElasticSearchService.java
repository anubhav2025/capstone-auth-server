package com.capstone.authServer.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.capstone.authServer.dto.ScanToolType;
import com.capstone.authServer.model.Finding;
import com.capstone.authServer.model.FindingSeverity;
import com.capstone.authServer.model.FindingState;
import com.capstone.authServer.model.SearchFindingsResult;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

@Service
public class ElasticSearchService {

    private final ElasticsearchClient esClient;

    public ElasticSearchService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    public void saveFinding(Finding finding) {
        try {
            esClient.index(i -> i.index("findings").id(finding.getId()).document(finding));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SearchFindingsResult searchFindings(ScanToolType toolType, FindingSeverity severity, FindingState state, int page, int size) {
        SearchResponse<Finding> response;
        try {
            response = esClient.search(s -> s
                    .index("findings")
                    .query(q -> q.bool(buildBoolQuery(toolType, severity, state)))
                    .sort(sort -> sort.field(f -> f
                    .field("updatedAt")           // <--- your field name in ES
                    .order(SortOrder.Desc)
                    ))
                    .from(page * size)
                    .size(size),
                    Finding.class);


                    long total = response.hits().total().value(); // total matching docs
                    List<Finding> results = response.hits().hits().stream()
                                            .map(Hit::source)
                                            .collect(Collectors.toList());
                    return new SearchFindingsResult(results, total);
        } catch (Exception e) {
            e.printStackTrace();
            return new SearchFindingsResult(List.of(), 0L);
        }
    }

    private BoolQuery buildBoolQuery(ScanToolType toolType, FindingSeverity severity, FindingState state) {
        return BoolQuery.of(b -> {
            if (toolType != null) {
                b.must(m -> m.term(t -> t.field("toolType.keyword").value(toolType.name())));
            }
            if (severity != null) {
                b.must(m -> m.term(t -> t.field("severity.keyword").value(severity.name())));
            }
            if (state != null) {
                b.must(m -> m.term(t -> t.field("state.keyword").value(state.name())));
            }
            return b;
        });
    }
}