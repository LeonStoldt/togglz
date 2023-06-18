package sample;

import com.google.cloud.spanner.DatabaseId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.Feature;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.UserProvider;
import org.togglz.googlecloudspanner.SpannerStateRepository;

@Configuration
public class TogglzConfiguration implements TogglzConfig {

    @Autowired
    private DatabaseId databaseId;

    @Bean
    public FeatureProvider featureProvider() {
        return new EnumBasedFeatureProvider(Features.class);
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return new EnumBasedFeatureProvider(Features.class);
    }

    @Override
    public StateRepository getStateRepository() {
        return new SpannerStateRepository(databaseId);
    }

    @Override
    public UserProvider getUserProvider() {
        return null;
    }
}
