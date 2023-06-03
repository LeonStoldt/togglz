package org.togglz.googlecloudspanner.repository;

import com.google.cloud.spanner.DatabaseId;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import static org.togglz.googlecloudspanner.config.FeatureInitializer.DEFAULT_FEATURE_STATE;

public class SpannerStateRepository implements StateRepository {

    private static final Logger log = LoggerFactory.getLogger(SpannerStateRepository.class);

    private static final String DEFAULT_TABLE_NAME = "FeatureToggle";

    private final FeatureRepository featureRepository;

    @Inject
    public SpannerStateRepository(String project, String instance, String database) {
        this(project, instance, database, DEFAULT_TABLE_NAME);
    }

    @Inject
    public SpannerStateRepository(String project, String instance, String database, String tableName) {
        var databaseId = DatabaseId.of(project, instance, database);
        this.featureRepository = new FeatureRepository(databaseId, tableName);
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        var databaseFeature = featureRepository.findByName(feature.name());
        if (databaseFeature.isPresent()) {
            var featureState = databaseFeature.get().enabled();
            log.debug("Found feature = {} with state enabled = {}", feature, featureState);
            return new FeatureState(feature, featureState);
        }
        log.warn("Feature state for feature = {} not found.", feature);
        return new FeatureState(feature, DEFAULT_FEATURE_STATE);
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        featureRepository.upsert(featureState);
        log.info("Saved state for feature = {} with enabled = {}", featureState.getFeature().name(), featureState.isEnabled());
    }
}
