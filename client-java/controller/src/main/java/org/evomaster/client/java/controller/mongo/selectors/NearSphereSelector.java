package org.evomaster.client.java.controller.mongo.selectors;

import org.evomaster.client.java.controller.mongo.operations.NearSphereOperation;
import org.evomaster.client.java.controller.mongo.operations.QueryOperation;

import java.util.Set;

import static org.evomaster.client.java.controller.mongo.utils.BsonHelper.*;

/**
 * Selectors for operations whose value consist of a single condition
 */
public class NearSphereSelector extends QuerySelector {
    @Override
    public QueryOperation getOperation(Object query) {
        String fieldName = extractFieldName(query);
        Object innerDoc = getValue(query, fieldName);

        if (!isDocument(innerDoc) || !hasTheExpectedOperator(query)) return null;

        Object point = getValue(innerDoc, operator());
        Object maxDistance = getValue(innerDoc, "$maxDistance");
        Object minDistance = getValue(innerDoc, "$minDistance");

        return parseValue(fieldName, point, maxDistance, minDistance);
    }

    protected String extractOperator(Object query) {
        String fieldName = extractFieldName(query);
        Set<String> keys = documentKeys(getValue(query, fieldName));
        return keys.stream().findFirst().orElse(null);
    }

    @Override
    protected String operator() {
        return "$nearSphere";
    }

    public QueryOperation parseValue(String fieldName, Object point, Object maxDistance, Object minDistance) {
        Object x = getValue(point, "x");
        Object y = getValue(point, "y");

        if (x instanceof Double
                && y instanceof Double
                && (maxDistance == null || maxDistance instanceof Double)
                && (minDistance == null || minDistance instanceof Double)) {

            return new NearSphereOperation(fieldName, (Double) x, (Double) y, (Double) maxDistance, (Double) minDistance);
        }
        return null;
    }

    private String extractFieldName(Object query) {
        Set<String> keys = documentKeys(query);
        return keys.stream().findFirst().orElse(null);
    }
}