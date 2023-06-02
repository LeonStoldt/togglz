package org.togglz.googlecloudspanner.repository;

import com.google.cloud.spanner.Statement;
import com.google.cloud.spring.data.spanner.core.SpannerOperations;
import com.google.cloud.spring.data.spanner.core.SpannerQueryOptions;
import com.google.cloud.spring.data.spanner.core.mapping.Table;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.util.DefaultMapSerializer;
import org.togglz.core.repository.util.MapSerializer;
import org.togglz.googlecloudspanner.entity.SpannerFeature;

import java.util.Optional;

@Lazy
@Component
public class FeatureRepository {
    private static final String SPANNER_FEATURE_TABLE = SpannerFeature.class.getAnnotation(Table.class).name();
    private static final String SELECT_FROM_FEATURE_TABLE_WHERE_NAME_IS_EQUAL = "SELECT * FROM " + SPANNER_FEATURE_TABLE + " WHERE name = @name";

    private final MapSerializer serializer;
    private final SpannerOperations spanner;

    public FeatureRepository(SpannerOperations spanner) {
        this.serializer = DefaultMapSerializer.multiline();
        this.spanner = spanner;
    }


    public Optional<SpannerFeature> findByName(String name) {
        return spanner.query(SpannerFeature.class,
                        Statement
                                .newBuilder(SELECT_FROM_FEATURE_TABLE_WHERE_NAME_IS_EQUAL)
                                .bind("name").to(name)
                                .build(),
                        new SpannerQueryOptions().setAllowPartialRead(true))
                .stream()
                .findFirst();
    }

    public void upsert(FeatureState featureState) {
        spanner.upsert(convertToEntity(featureState));
    }

    private SpannerFeature convertToEntity(FeatureState featureState) {
        return new SpannerFeature(
                featureState.getFeature().name(),
                featureState.isEnabled(),
                featureState.getStrategyId(),
                serializeParameters(featureState)
        );
    }

    private String serializeParameters(FeatureState featureState) {
        return serializer.serialize(featureState.getParameterMap());
    }
}
