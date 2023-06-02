package org.togglz.googlecloudspanner.config;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Component
public class EnvCheck {

    private static final Set<String> DEPLOYMENT_PROFILES = Set.of("dev", "stage", "live");

    private final Environment environment;

    public EnvCheck(Environment environment) {
        this.environment = environment;
    }

    public boolean isDeployed() {
        return Arrays.stream(environment.getActiveProfiles()).anyMatch(DEPLOYMENT_PROFILES::contains);
    }
}
