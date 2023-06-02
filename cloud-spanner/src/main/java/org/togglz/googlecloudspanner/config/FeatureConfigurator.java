package org.togglz.googlecloudspanner.config;

import org.springframework.stereotype.Component;
import org.togglz.core.Feature;

@Component
public interface FeatureConfigurator {
    Class<? extends Enum<? extends Feature>> getFeature();
}
