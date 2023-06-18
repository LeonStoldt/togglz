package org.togglz.googlecloudspanner;

import com.google.cloud.spanner.DatabaseId;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import static org.togglz.googlecloudspanner.config.FeatureInitializer.DEFAULT_FEATURE_STATE;

/**
 * <p>
 * This state repository implementation handles feature state access and persistence
 * in <a href="https://cloud.google.com/spanner/docs/">Google Cloud Spanner</a>
 * </p>
 *
 * <p>
 * {@link SpannerStateRepository} stores the feature state in a separate table called FeatureToggle by default.
 * If you want to use a custom table name, it can be provided as parameter in the constructor.
 * </p>
 *
 * @author Leon Stoldt
 */
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
        this(DatabaseId.of(project, instance, database), tableName);
    }

    @Inject
    public SpannerStateRepository(DatabaseId databaseId) {
        this(databaseId, DEFAULT_TABLE_NAME);
    }

    @Inject
    public SpannerStateRepository(DatabaseId databaseId, String tableName) {
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
        log.warn("Feature state for feature = {} not found. Using default feature state = {}", feature, DEFAULT_FEATURE_STATE);
        return new FeatureState(feature, DEFAULT_FEATURE_STATE);
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        featureRepository.upsert(featureState);
        log.info("Saved state for feature = {} with enabled = {}", featureState.getFeature().name(), featureState.isEnabled());
    }
}
