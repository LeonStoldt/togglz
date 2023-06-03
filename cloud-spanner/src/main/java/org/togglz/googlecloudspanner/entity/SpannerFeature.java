package org.togglz.googlecloudspanner.entity;

public record SpannerFeature(
        String name,
        boolean enabled,
        String strategyId,
        String strategyParameters
) {}
