package org.evomaster.client.java.controller.mongo.operations;

/**
 * Represent $nearSphere operation.
 * Specifies a point for which a geospatial query returns the documents from nearest to farthest.
 */
public class NearSphereOperation extends QueryOperation{
    private final String fieldName;
    private final Double x;

    private final Double y;

    private final Double maxDistance;

    private final Double minDistance;


    public NearSphereOperation(String fieldName, Double x, Double y, Double maxDistance, Double minDistance) {
        this.fieldName = fieldName;
        this.x = x;
        this.y = y;
        this.maxDistance = maxDistance;
        this.minDistance = minDistance;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Double getMaxDistance() {
        return maxDistance;
    }

    public Double getMinDistance() {
        return minDistance;
    }
}