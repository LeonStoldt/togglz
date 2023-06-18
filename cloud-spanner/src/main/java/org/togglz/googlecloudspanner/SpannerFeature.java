package org.togglz.googlecloudspanner;

public record SpannerFeature(
        String name,
        boolean enabled,
        String strategyId,
        String strategyParameters
) {}
