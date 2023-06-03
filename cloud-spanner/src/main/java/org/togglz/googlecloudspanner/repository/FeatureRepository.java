package org.togglz.googlecloudspanner.repository;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.util.DefaultMapSerializer;
import org.togglz.core.repository.util.MapSerializer;
import org.togglz.googlecloudspanner.entity.SpannerFeature;

import java.util.Objects;
import java.util.Optional;

public class FeatureRepository {
    public static final String SELECT_FEATURE_FROM = "SELECT * FROM ";
    public static final String WHERE_NAME_IS_EQUAL = " WHERE name = @name";
    public static final String OPTIMIZER_VERSION_ENABLE_PARTIAL_READS = "2"; //https://cloud.google.com/spanner/docs/query-optimizer/versions

    private final MapSerializer serializer;
    private final DatabaseClient spannerClient;

    private final String tableName;

    public FeatureRepository(DatabaseId databaseId, String tableName) {
        if (Objects.isNull(tableName) || tableName.isBlank()) {
            throw new IllegalArgumentException("Spanner tableName for togglz cannot be null or empty.");
        }

        this.serializer = DefaultMapSerializer.multiline();
        this.spannerClient = getSpannerClientByDatabaseId(databaseId);
        this.tableName = tableName;
    }

    private DatabaseClient getSpannerClientByDatabaseId(DatabaseId databaseId) {
        var spannerOptions = SpannerOptions.newBuilder().build();
        var spanner = spannerOptions.getService();
        return spanner.getDatabaseClient(databaseId);
    }

    public Optional<SpannerFeature> findByName(String name) {
        try (var readOnlyTransaction = spannerClient.singleUse()) {
            var queryResult = readOnlyTransaction.executeQuery(getSelectStatement(name));
            if (queryResult.next()) {
                var enabled = queryResult.getBoolean("enabled");
                var strategyId = queryResult.getString("strategyId");
                var strategyParameters = queryResult.getString("strategyParameters");
                var feature = new SpannerFeature(name, enabled, strategyId, strategyParameters);
                return Optional.of(feature);
            }
        }
        return Optional.empty();
    }

    private Statement getSelectStatement(String name) {
        var statement = Statement
                .newBuilder(SELECT_FEATURE_FROM).append(tableName).append(WHERE_NAME_IS_EQUAL)
                .bind("name").to(name)
                .build();

        return getStatementWithPartialReadEnabled(statement);
    }

    private static Statement getStatementWithPartialReadEnabled(Statement statement) {
        var queryOptions = statement.getQueryOptions().toBuilder()
                .setOptimizerVersion(OPTIMIZER_VERSION_ENABLE_PARTIAL_READS)
                .build();
        return statement
                .toBuilder()
                .withQueryOptions(queryOptions)
                .build();
    }

    public void upsert(FeatureState featureState) {
        Mutation.newInsertOrUpdateBuilder(tableName)
                .set("name").to(featureState.getFeature().name())
                .set("enabled").to(featureState.isEnabled())
                .set("strategyId").to(featureState.getStrategyId())
                .set("strategyParameters").to(serializeParameters(featureState))
                .build();
    }

    private String serializeParameters(FeatureState featureState) {
        return serializer.serialize(featureState.getParameterMap());
    }
}
