package org.togglz.googlecloudspanner.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.Feature;
import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.googlecloudspanner.repository.SpannerStateRepository;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnBean(FeatureConfigurator.class)
public class FeatureConfig {
    private static final TimeUnit FEATURE_STATE_CACHE_TTL_UNIT = TimeUnit.SECONDS;

    private static final Logger log = LoggerFactory.getLogger(FeatureConfig.class);

    @Value("${feature.cache.ttl-in-s:30}")
    private int featureStateCacheTtl;
    private final FeatureConfigurator featureConfigurator;
    private final ApplicationContext context;
    private final EnvCheck envCheck;

    public FeatureConfig(FeatureConfigurator featureConfigurator, ApplicationContext context, EnvCheck envCheck) {
        this.featureConfigurator = featureConfigurator;
        this.context = context;
        this.envCheck = envCheck;
    }

    @PostConstruct
    public void initTogglz() {
        // currently togglz only supports one feature class
        var feature = featureConfigurator.getFeature();
        StateRepository repository;

        if (envCheck.isDeployed()) {
            repository = context.getBean(SpannerStateRepository.class);
            if (featureStateCacheTtl > 0L) {
                repository = new CachingStateRepository(repository, featureStateCacheTtl, FEATURE_STATE_CACHE_TTL_UNIT);
            }
            initializeFeatures(feature);
            log.info("Togglz initialized with spanner compatible state repository and feature state cache with TTL of {} {}.",
                    featureStateCacheTtl, FEATURE_STATE_CACHE_TTL_UNIT);
        } else {
            repository = new InMemoryStateRepository();
            log.info("Togglz initialized with in-memory state repository");
        }

        StaticFeatureManagerProvider.setFeatureManager(
                FeatureManagerBuilder
                        .begin()
                        .featureProvider(new EnumBasedFeatureProvider(feature.asSubclass(Feature.class)))
                        .stateRepository(repository)
                        .build()
        );
    }

    private void initializeFeatures(Class<? extends Enum<? extends Feature>> featureEnum) {
        var featureInitializer = context.getBean(FeatureInitializer.class);
        for (var feature : featureEnum.getEnumConstants()) {
            featureInitializer.initializeFeature((Feature) feature);
        }
    }
}
