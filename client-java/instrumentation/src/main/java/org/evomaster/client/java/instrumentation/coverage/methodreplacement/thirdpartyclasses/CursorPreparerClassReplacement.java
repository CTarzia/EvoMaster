package org.evomaster.client.java.instrumentation.coverage.methodreplacement.thirdpartyclasses;

import org.evomaster.client.java.instrumentation.MongoInfo;
import org.evomaster.client.java.instrumentation.coverage.methodreplacement.Replacement;
import org.evomaster.client.java.instrumentation.coverage.methodreplacement.ThirdPartyCast;
import org.evomaster.client.java.instrumentation.coverage.methodreplacement.ThirdPartyMethodReplacementClass;
import org.evomaster.client.java.instrumentation.coverage.methodreplacement.UsageFilter;
import org.evomaster.client.java.instrumentation.object.ClassToSchema;
import org.evomaster.client.java.instrumentation.shared.ReplacementCategory;
import org.evomaster.client.java.instrumentation.shared.ReplacementType;
import org.evomaster.client.java.instrumentation.staticstate.ExecutionTracer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class CursorPreparerClassReplacement extends ThirdPartyMethodReplacementClass {
    private static final CursorPreparerClassReplacement singleton = new CursorPreparerClassReplacement();

    @Override
    protected String getNameOfThirdPartyTargetClass() {
        return "org.springframework.data.mongodb.core.CursorPreparer";
    }

    @Replacement(replacingStatic = false, type = ReplacementType.TRACKER, id = "initiateFind", usageFilter = UsageFilter.ANY, category = ReplacementCategory.MONGO, castTo = "com.mongodb.client.FindIterable")
    public static Object initiateFind(Object preparer, @ThirdPartyCast(actualType = "com.mongodb.client.MongoCollection") Object mongoCollection, Function<Object, Object> find) {
        return handleFind(mongoCollection, find);
    }

    private static Object handleFind(Object mongoCollection, Function<Object, Object> find) {
        long startTime = System.currentTimeMillis();

        Object argument = getField(find, "arg$1");
        Object query = getField(argument, "query");
        Object result = find.apply(mongoCollection);

        long endTime = System.currentTimeMillis();

        handleMongo(mongoCollection, query, true, endTime - startTime);

        return result;
    }

    private static Object getField(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleMongo(Object mongoCollection, Object bson, boolean successfullyExecuted, long executionTime) {
        String schema = ClassToSchema.getOrDeriveSchemaWithItsRef(extractDocumentsType(mongoCollection), true);
        MongoInfo info = new MongoInfo(getCollectionName(mongoCollection), getDatabaseName(mongoCollection), schema, getDocuments(mongoCollection), bson, successfullyExecuted, executionTime);
        ExecutionTracer.addMongoInfo(info);
    }


    private static Iterable<?> getDocuments(Object collection) {
        // Need to convert result of getDocuments which a FindIterable instance as it is not Serializable
        List<Object> documentsAsList = new ArrayList<>();
        try {
            Class<?> collectionClass = getCollectionClass(collection);
            Iterable<?> findIterable = (Iterable<?>) collectionClass.getMethod("find").invoke(collection);
            findIterable.forEach(documentsAsList::add);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new RuntimeException("Failed to retrieve all documents from a mongo collection", e);
        }
        return documentsAsList;
    }

    private static Class<?> extractDocumentsType(Object collection) {
        try {
            Class<?> collectionClass = getCollectionClass(collection);
            return (Class<?>) collectionClass.getMethod("getDocumentClass").invoke(collection);
        } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException("Failed to retrieve document's type from collection", e);
        }
    }

    private static String getDatabaseName(Object collection) {
        try {
            Class<?> collectionClass = getCollectionClass(collection);
            Object namespace = collectionClass.getMethod("getNamespace").invoke(collection);
            return (String) namespace.getClass().getMethod("getDatabaseName").invoke(namespace);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new RuntimeException("Failed to retrieve name of the database in which collection is", e);
        }
    }

    private static String getCollectionName(Object collection) {
        try {
            Class<?> collectionClass = getCollectionClass(collection);
            Object namespace = collectionClass.getMethod("getNamespace").invoke(collection);
            return (String) namespace.getClass().getMethod("getCollectionName").invoke(namespace);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new RuntimeException("Failed to retrieve collection name", e);
        }
    }

    private static Class<?> getCollectionClass(Object collection) throws ClassNotFoundException {
        // collection is an implementation of interface MongoCollection
        return collection.getClass().getInterfaces()[0];
    }
}
