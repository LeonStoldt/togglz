package org.togglz.googlecloudspanner.repository;

import com.google.cloud.spring.data.spanner.core.SpannerOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import static org.togglz.googlecloudspanner.config.FeatureInitializer.DEFAULT_FEATURE_STATE;

@Lazy
@Component
public class SpannerStateRepository implements StateRepository {

    private static final Logger log = LoggerFactory.getLogger(SpannerStateRepository.class);

    private final FeatureRepository featureRepository;

    public SpannerStateRepository(SpannerOperations spannerOperations) {
        this.featureRepository = new FeatureRepository(spannerOperations);
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        var databaseFeature = featureRepository.findByName(feature.name());
        var featureState = DEFAULT_FEATURE_STATE;

        if (databaseFeature.isPresent()) {
            featureState = databaseFeature.get().isEnabled();
            log.debug("Found feature = {} with state enabled = {}", feature, featureState);
        } else {
            log.warn("Feature state for feature = {} not found.", feature);
        }

        return new FeatureState(feature, featureState);
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        featureRepository.upsert(featureState);
        log.info("Saved state for feature = {} with enabled = {}", featureState.getFeature().name(), featureState.isEnabled());
    }
}
