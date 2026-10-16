package com.university.booking.service;

import com.university.booking.dto.ExplainComparison;
import com.university.booking.dto.ExplainSummary;
import com.university.booking.enums.BookingStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExplainService {

    private final MongoTemplate mongoTemplate;

    public ExplainComparison explainAuthorStatus(String personId, BookingStatus status) {
        Document filter = new Document("personId", personId).append("status", status.name());
        Document sort = new Document("createdAt", -1);

        return new ExplainComparison(filter, sort,
                explain(filter, sort, new Document("$natural", 1)),
                explain(filter, sort, null));
    }

    private ExplainSummary explain(Document filter, Document sort, Document hint) {
        Document find = new Document("find", "bookings")
                .append("filter", filter)
                .append("sort", sort);
        if (hint != null) {
            find.append("hint", hint);
        }
        Document result = mongoTemplate.executeCommand(new Document("explain", find)
                .append("verbosity", "executionStats"));

        Document planner = result.get("queryPlanner", Document.class);
        Document winningPlan = planner.get("winningPlan", Document.class);
        if (winningPlan.containsKey("queryPlan")) {
            winningPlan = winningPlan.get("queryPlan", Document.class);
        }
        List<String> stages = new ArrayList<>();
        String[] indexName = new String[1];
        collectStages(winningPlan, stages, indexName);

        Document stats = result.get("executionStats", Document.class);
        return new ExplainSummary(
                String.join(" <- ", stages),
                indexName[0],
                number(stats.get("nReturned")),
                number(stats.get("totalKeysExamined")),
                number(stats.get("totalDocsExamined")),
                number(stats.get("executionTimeMillis")));
    }

    private static void collectStages(Document plan, List<String> stages, String[] indexName) {
        stages.add(plan.getString("stage"));
        if (plan.getString("indexName") != null) {
            indexName[0] = plan.getString("indexName");
        }
        Document input = plan.get("inputStage", Document.class);
        if (input != null) {
            collectStages(input, stages, indexName);
        }
        List<Document> inputs = plan.getList("inputStages", Document.class);
        if (inputs != null) {
            inputs.forEach(stage -> collectStages(stage, stages, indexName));
        }
    }

    private static long number(Object value) {
        return value instanceof Number n ? n.longValue() : 0;
    }
}