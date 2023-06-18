# Google Cloud Spanner Integration
> Sample Service can be found [here](../samples/spring-boot-cloud-spanner).

## Client Service Permissions

Ensure that the client service account has at least the **role** `roles/spanner.databaseReader` for accessing feature state (read only).

To create new features or update the feature state, ensure the service account has the **role** `roles/spanner.databaseUser`.

## Behaviour

- By default the spanner table for storing feature states is called `FeatureToggle`.
- If a feature in not present in the table, it will be created with the default value of `false`.

## Getting started

### 1. Add dependencies

Import the required dependencies in your `pom.xml`:

```xml
<dependencies>
    <!-- Togglz Depencencies -->
    <dependency>
        <groupId>org.togglz</groupId>
        <artifactId>togglz-core</artifactId>
        <version>${togglz-version}</version>
    </dependency>
    <dependency>
        <groupId>org.togglz</groupId>
        <artifactId>togglz-cloud-spanner</artifactId>
        <version>${togglz-version}</version>
    </dependency>


    <!-- Test Dependencies -->
    <dependency>
        <groupId>org.togglz</groupId>
        <artifactId>togglz-junit</artifactId>
        <version>${togglz.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>

```
### 2. Add toggle enum

Create a togglz enum for the feature you want to use:

```java
import org.togglz.core.Feature;

public enum MyFeatureTogglz implements Feature {
    FEATURE_ONE,
    FEATURE_TWO
}
```

### 3. Add configuration class

```java
import org.springframework.stereotype.Component;
import org.togglz.core.Feature;

public class TogglzFeatureConfigurator implements FeatureConfigurator {

    @Override
    public Class<? extends Enum<? extends Feature>> getFeature() {
        return TogglzFeature.class;
    }
}
```

### 4. Toggle your code!

```java
public class Test {
    public void decideOnToggleState() {
        if (FEATURE_ONE.isActive()) {
            doThis();
        } else {
            doThat();
        }
    }
}
```
