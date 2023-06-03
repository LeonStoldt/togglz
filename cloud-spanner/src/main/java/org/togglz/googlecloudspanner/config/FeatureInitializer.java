package org.togglz.googlecloudspanner.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.googlecloudspanner.repository.FeatureRepository;

public class FeatureInitializer {
    private static final Logger log = LoggerFactory.getLogger(FeatureInitializer.class);
    public static final boolean DEFAULT_FEATURE_STATE = false;
    private final FeatureRepository featureRepository;

    public FeatureInitializer(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    public void initializeFeature(Feature feature) {
        if (featureRepository.findByName(feature.name()).isEmpty()) {
            featureRepository.upsert(new FeatureState(feature, DEFAULT_FEATURE_STATE));
            log.info("Initialized togglz feature {} with state enabled = {}.", feature.name(), DEFAULT_FEATURE_STATE);
        }
    }
}
