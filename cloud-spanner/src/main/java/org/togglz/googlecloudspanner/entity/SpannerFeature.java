package org.togglz.googlecloudspanner.entity;

import com.google.cloud.spring.data.spanner.core.mapping.PrimaryKey;
import com.google.cloud.spring.data.spanner.core.mapping.Table;

@Table(name = "togglzFeature")
public class SpannerFeature {
    @PrimaryKey
    private String name;
    private boolean enabled;
    private String strategyId;
    private String strategyParameters;

    public SpannerFeature() {
    }

    public SpannerFeature(String name, boolean enabled, String strategyId, String strategyParameters) {
        this.name = name;
        this.enabled = enabled;
        this.strategyId = strategyId;
        this.strategyParameters = strategyParameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

    public String getStrategyParameters() {
        return strategyParameters;
    }

    public void setStrategyParameters(String strategyParameters) {
        this.strategyParameters = strategyParameters;
    }
}
