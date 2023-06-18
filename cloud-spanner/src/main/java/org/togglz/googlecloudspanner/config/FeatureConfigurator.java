package org.togglz.googlecloudspanner.config;

import org.togglz.core.Feature;

public interface FeatureConfigurator {
    Class<? extends Enum<? extends Feature>> getFeature();
}
