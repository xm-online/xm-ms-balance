# Migration Guide: Spring Boot 4.0.4 / Java 25 / Jackson 3 / Hibernate 7

Use this to migrate other XM microservices.

---

## 1. Gradle & Build System

### Gradle 8.9 -> 9.2.1
- Update `gradle/wrapper/gradle-wrapper.properties`:
  ```properties
  distributionUrl=https://services.gradle.org/distributions/gradle-9.2.1-bin.zip
  ```
- Run `./gradlew wrapper` to regenerate wrapper scripts

### Gradle API changes
- Replace `task myTask(type: X) {}` with `tasks.register('myTask', X) {}`
- Replace `destinationDir` with `destinationDirectory` (use `layout.buildDirectory.dir(...)`)
- Replace `reportOn` with `testResults.from()`
- Replace `rootProject.buildDir` with `rootProject.layout.buildDirectory`
- Remove full import for `JavadocMemberLevel`, use direct reference

### Plugin updates
| Plugin | Old | New |
|--------|-----|-----|
| dependency-management-plugin | 1.0.11.RELEASE | 1.1.7 |
| net.saliman.properties | 1.5.2 | 1.6.0 |
| com.github.spotbugs | 5.0.9 | 6.4.8 |

### Java 25 module system compatibility
Add JVM args for tests (required for Mockito with Java 25):
```gradle
jvmArgs += [
    '--add-opens', 'java.base/java.lang=ALL-UNNAMED',
    '--add-opens', 'java.base/java.lang.reflect=ALL-UNNAMED',
    '--add-opens', 'java.base/java.util=ALL-UNNAMED'
]
```

### Remove custom Groovy version
Spring Boot 4 manages Groovy internally. Remove any custom Groovy version pinning (e.g., `groovy_version` in gradle.properties or resolution strategy overrides).

### Update git-properties plugin
```properties
gitPropertiesPluginVersion=2.5.4
```

### Java version config
Update explicit Java version if present:
```gradle
// Old
sourceCompatibility = 21
targetCompatibility = 21

// New
java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}
```

### CI/CD and Docker
- Update Java version in `.github/workflows/*.yml`: `java-version: '21'` -> `java-version: '25'`
- Update `.travis.yml`: `oraclejdk21` -> `oraclejdk25`
- Update Docker base image: `eclipse-temurin:21-jre` -> `eclipse-temurin:25-jre`
---

## 2. Spring Boot 3.3 -> 4.0.4

### Spring Cloud
- `2023.0.4` -> `2025.1.0`

### Package relocations

| Old | New |
|-----|-----|
| `org.springframework.boot.autoconfigure.kafka.KafkaProperties` | `org.springframework.boot.kafka.autoconfigure.KafkaProperties` |
| `org.springframework.boot.web.client.RestTemplateBuilder` | `org.springframework.boot.restclient.RestTemplateBuilder` |

### KafkaProperties API
```java
// Old
kafkaProperties.buildConsumerProperties(null)

// New
kafkaProperties.buildConsumerProperties()
```

### RestTemplateBuilder API
```java
// Old
restTemplateBuilder.setConnectTimeout(duration)
restTemplateBuilder.setReadTimeout(duration)

// New
restTemplateBuilder.connectTimeout(duration)
restTemplateBuilder.readTimeout(duration)
```
**Important:** `RestTemplateBuilder` is immutable - these methods return a new builder. Capture the result:
```java
restTemplateBuilder = restTemplateBuilder
    .connectTimeout(duration)
    .readTimeout(duration);
```

### DefaultResponseErrorHandler
```java
// Old
@Override
public void handleError(ClientHttpResponse response) throws IOException { ... }

// New
@Override
public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException { ... }
```

### WebMvcConfigurer
- Remove `configurePathMatch()` / `setUseSuffixPatternMatch()` (removed in Spring 6+)

### Kafka starter
Add explicit Kafka dependencies where needed:
```gradle
implementation("org.springframework.boot:spring-boot-starter-kafka")
implementation("org.springframework.retry:spring-retry")
```

### RestClient
Add where needed:
```gradle
implementation("org.springframework.boot:spring-boot-restclient")
```

### Tomcat exclusion
Remove `exclude module: 'spring-boot-starter-tomcat'` from `spring-boot-starter-web` (no longer needed).

### LiquibaseProperties relocation
```java
// Old
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;

// New
import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;
```

### WebMvcTest relocation
```java
// Old
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

// New
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
```

### MappingJackson2HttpMessageConverter -> JacksonJsonHttpMessageConverter
```java
// Old
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

// New
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
```

### WebMvcConfigurer.configureMessageConverters signature change
```java
// Old
@Override
public void configureMessageConverters(List<HttpMessageConverter<?>> converters) { ... }

// New
@Override
public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
    builder.configureMessageConvertersList(converters -> { ... });
}
```

### AsyncConfiguration bean name
```java
// Old
@Bean(name = "taskExecutor")

// New
@Bean(name = "applicationTaskExecutor")
```
Update all `@Qualifier("taskExecutor")` references accordingly.

### ContentCachingWrappingFilter
```java
// Old
new ContentCachingRequestWrapper(request)

// New
new ContentCachingRequestWrapper(request, 0)
```

### WebConfigurer cleanup
Remove `ServerProperties` dependency from `WebConfigurer`. Remove Undertow HTTP/2 configuration.

### HttpClient5 / HttpCore5 versions
```properties
httpClient5Version=5.6.1
httpCore5Version=5.4.2
```
Add dependency if needed:
```gradle
implementation "org.apache.httpcomponents.core5:httpcore5:${httpCore5Version}"
```

### OAuth2 starters
Add if your project uses OAuth2:
```gradle
implementation "org.springframework.boot:spring-boot-starter-security-oauth2-client"
implementation "org.springframework.boot:spring-boot-starter-security-oauth2-resource-server"
```

---

## 3. Jackson 2.x -> 3.x (fasterxml -> tools.jackson)

### IMPORTANT: Backward Compatibility

Jackson 2 (`com.fasterxml.jackson`) and Jackson 3 (`tools.jackson`) are **completely separate class hierarchies**. They can coexist on the classpath, but types are NOT interchangeable.

**What this means for consumer projects:**

1. **xm-commons public API now uses Jackson 3 types.** If your code extends `AbstractRefreshableConfiguration`, `SpecProcessor`, or calls `JsonMapperUtils`/`YamlMapperUtils`, you MUST use `tools.jackson` imports. `com.fasterxml.jackson.databind.ObjectMapper` cannot be assigned from `tools.jackson.databind.ObjectMapper`.

2. **Spring Boot 4 auto-configures only Jackson 3 ObjectMapper bean.** `@Autowired ObjectMapper` will inject `tools.jackson.databind.ObjectMapper`. If your code imports `com.fasterxml.jackson.databind.ObjectMapper`, it won't match. Either change the import or manually create a Jackson 2 bean.

3. **Jackson 2 annotations are invisible to Jackson 3 ObjectMapper.** If your domain classes have `@com.fasterxml.jackson.annotation.JsonIgnore` etc., those annotations will be **ignored** by Jackson 3. Use dual annotations if you need both:
   ```java
   @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = OldSerializer.class)
   @tools.jackson.databind.annotation.JsonSerialize(using = NewSerializer.class)
   public class MyEntity { ... }
   ```

4. **JacksonException is now unchecked** (`RuntimeException`). Any `catch (IOException)` around Jackson operations will **NOT catch** Jackson 3 exceptions. Use `catch (JacksonException)`.

5. **Both jars must be on classpath** during transition. Keep `com.fasterxml.jackson` dependencies while you have any Jackson 2 code (old serializers, annotations, LEP scripts).

### Maven/Gradle coordinates
```gradle
// Old: com.fasterxml.jackson.*
// New: tools.jackson.*
implementation("tools.jackson.core:jackson-databind")
implementation("tools.jackson.core:jackson-core")
implementation("tools.jackson.dataformat:jackson-dataformat-yaml")
```

### Package rename (all imports)
| Old | New |
|-----|-----|
| `com.fasterxml.jackson.core.*` | `tools.jackson.core.*` |
| `com.fasterxml.jackson.databind.*` | `tools.jackson.databind.*` |
| `com.fasterxml.jackson.dataformat.yaml.*` | `tools.jackson.dataformat.yaml.*` |
| `com.fasterxml.jackson.core.JsonFactory` | `tools.jackson.core.json.JsonFactory` |

### ObjectMapper creation
Jackson 3 ObjectMapper is immutable. Use builders:
```java
// Old
new ObjectMapper()
new ObjectMapper(new YAMLFactory())

// New
JsonMapper.builder().build()
YAMLMapper.builder().build()
```

Or use shared utility classes like `JsonMapperUtils` / `YamlMapperUtils`:
```java
JsonMapperUtils.getDefaultJsonMapper()
JsonMapperUtils.getJsonMapperWithIgnore()  // FAIL_ON_UNKNOWN_PROPERTIES disabled
YamlMapperUtils.yamlDefaultMapper()
```

### ObjectMapper.copy() -> rebuild().build()
```java
// Old
ObjectMapper copy = objectMapper.copy();

// New
ObjectMapper copy = objectMapper.rebuild().build();
```

### Configuring features with builders
```java
// Old
objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

// New (use builder)
JsonMapper.builder()
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .build();

// Or with configure() on builder (works - mutates builder in place)
builder.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
```

### JsonNode API changes
| Old | New |
|-----|-----|
| `jsonNode.asText()` | `jsonNode.asString()` or `jsonNode.asString("")` |
| `jsonNode.findValuesAsText("field")` | `jsonNode.findValuesAsString("field")` |
| `objectNode.fields()` | `objectNode.properties().iterator()` |
| `arrayNode.elements()` | `arrayNode.values()` (or `arrayNode.iterator()`) |

### JavaTimeModule removed
Jackson 3 has built-in Java 8 date/time support. Remove:
```java
// Delete this import and usage entirely
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
objectMapper.registerModule(new JavaTimeModule());
```

### SerializationFeature.INDENT_OUTPUT
```java
// Old
objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

// New (Jackson 3 - use writer directly)
objectMapper.writerWithDefaultPrettyPrinter();
```

### jackson-datatype-hibernate7 coordinate change
```gradle
// Old (fasterxml group)
implementation "com.fasterxml.jackson.datatype:jackson-datatype-hibernate7"

// New (tools.jackson group)
implementation "tools.jackson.datatype:jackson-datatype-hibernate7"
```

### ObjectMapper bean customization pattern (Spring Boot 4)
```java
@Bean
@ConditionalOnMissingBean(ObjectMapper.class)
public JsonMapper objectMapper(List<JsonMapperBuilderCustomizer> customizers) {
    JsonMapper.Builder jsonBuilder = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION);

    customizers.forEach(c -> c.customize(jsonBuilder));
    return jsonBuilder.build();
}
```

### Validation message path format change
JSON schema validation messages changed format:
```
// Old (Jackson 2 / networknt 1.x)
$.fieldName

// New (Jackson 3 / networknt 3.x)
/fieldName
```
Update any tests or logic that parses validation error paths.

### Swagger date-time type change
Swagger-generated JSON schemas now represent date-time fields as `{"type": "string", "format": "date-time"}` instead of numeric timestamps.

### TypeFactory
```java
// Old
TypeFactory.defaultInstance()

// New
TypeFactory.createDefaultInstance()
```

### JsonParser creation
```java
// Old
jsonFactory.createParser(json)
jParser.getCurrentName()

// New
jsonFactory.createParser(ObjectReadContext.empty(), json)
jParser.currentName()
```

### Exception handling
```java
// Old
catch (IOException e) { ... }
catch (JsonProcessingException e) { ... }

// New
catch (JacksonException e) { ... }
```
`JacksonException` is now a `RuntimeException` (unchecked) in Jackson 3.

---

## 4. Hibernate  6.5 -> 7.3.1.Final

### Hibernate dependency
```gradle
// Old
hibernate: '6.5.3.Final'

// New
hibernate: '7.3.1.Final'
```

### Remove hibernate-types / hypersistence-utils
Remove `hibernateTypes60` dependency (vladmihalcea/hibernate-types) and `io.hypersistence:hypersistence-utils-hibernate-63`. They are no longer compatible with Hibernate 7.

### jackson-datatype-hibernate
```gradle
// Old
implementation "com.fasterxml.jackson.datatype:jackson-datatype-hibernate6"

// New
implementation "com.fasterxml.jackson.datatype:jackson-datatype-hibernate7"
```

### JPA Metamodel interface changes
If you have custom Metamodel implementations (e.g., in tests), update method signatures:
- Add `entity(String s)` method
- Collection-returning methods should return `Set.of()` instead of `null`

---

## 5. OAuth2 Client (Spring Security)

### Token response client
```java
// Old
DefaultClientCredentialsTokenResponseClient client = new DefaultClientCredentialsTokenResponseClient();
client.setRequestEntityConverter(new MyEntityConverter());

// New
RestClientClientCredentialsTokenResponseClient client = new RestClientClientCredentialsTokenResponseClient();
client.addHeadersConverter(new MyHeadersConverter());
```

### Converter pattern change
Replace entity converters with headers converters:
```java
// Old: implements Converter<OAuth2ClientCredentialsGrantRequest, RequestEntity<?>>
// New: implements Converter<OAuth2ClientCredentialsGrantRequest, HttpHeaders>

// Old imports
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequestEntityConverter;

// New imports
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestHeadersConverter;
```

---

## 6. JUnit 4 -> JUnit 5

### Annotations
| Old (JUnit 4) | New (JUnit 5) |
|----------------|---------------|
| `@RunWith(SpringRunner.class)` | `@ExtendWith(SpringExtension.class)` |
| `@Before` | `@BeforeEach` |
| `@After` | `@AfterEach` |
| `@Test` (org.junit) | `@Test` (org.junit.jupiter.api) |
| `@MockBean` | `@MockitoBean` |

### Imports
```java
// Old
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.boot.test.mock.mockito.MockBean;

// New
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
```

### Gradle test config
```gradle
// Add if not present
test {
    useJUnitPlatform()
}

// Dependencies
testImplementation('org.junit.jupiter:junit-jupiter')
testRuntimeOnly('org.junit.platform:junit-platform-launcher')
// Remove:
testImplementation('junit:junit')
```

### MockitoAnnotations
```java
// Old (deprecated in Mockito 5+)
MockitoAnnotations.initMocks(this);

// New
MockitoAnnotations.openMocks(this);
// Or use @ExtendWith(MockitoExtension.class)
```

---

## 7. Spring Data JPA

### Specification.where(null) no longer valid
```java
// Old
Specification.where(null)

// New
Specification.where(alwaysTrue())

private static <T> Specification<T> alwaysTrue() {
    return (root, query, cb) -> cb.conjunction();
}
```

---

## 8. Other Dependency Updates

| Dependency | Old | New |
|-----------|-----|-----|
| JaCoCo | 0.8.12 | 0.8.14 |
| Checkstyle | 10.12.5 | 13.4.0 |
| SpotBugs | 4.8.3 | 4.9.8 |
| PMD | 6.48.0 | 7.23.0 |
| EqualsVerifier | 3.16 | 3.19.1 |
| Commons Codec | 1.16.0 | 1.17.2 |
| Commons IO | 2.15.1 | 2.21.0 |
| Commons Collections4 | 4.4 | 4.5.0 |
| Commons Text | 1.11.0 | 1.13.0 |
| Guava | 33.0.0-jre | 33.4.0-jre |
| Lombok | 1.18.32 | 1.18.42 |
| Logstash Logback Encoder | 7.4 | 8.0 |
| Caffeine | 3.1.8 | 3.2.0 |
| Swagger Codegen | 3.0.51 | 3.0.68 |
| Hibernate Validation | 8.0.1.Final | 8.0.2.Final |
| MapStruct | 1.5.5.Final | 1.6.3 |
| Dropwizard Metrics | 4.2.25 | 4.2.31 |
| Lucene | 9.11.1 | 10.4.0 |
| JSON Schema Validator | 1.5.2 | 3.0.2 |

### JHipster
```properties
# Old
jhipsterDependenciesVersion=8.6.0

# New
jhipsterFrameworkDependenciesVersion=9.0.0
```
Note: `implementation platform(...)` may need to change to direct `implementation` depending on BOM availability.

### JSON Schema Validator (networknt) 1.5 -> 3.0
```java
// Old
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecificationVersion.DRAFT_4);
JsonSchema schema = factory.getSchema(schemaString);
Set<ValidationMessage> errors = schema.validate(jsonNode);

// New
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.Error;

SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
Schema schema = registry.getSchema(schemaString);
Set<Error> errors = schema.validate(jsonNode);
```

### PMD 6 -> 7
PMD 7 has breaking changes in rule configurations. Checkstyle/PMD tasks may need config updates (they were disabled with FIXME in xm-commons).

---

## 9. Jackson Custom Serializers Migration

### Dual-annotation approach (xm-ms-entity pattern)
If you need to support both Jackson 2 and 3 during transition, domain classes can have both annotations:
```java
// Support both Jackson 2 (com.fasterxml) and Jackson 3 (tools.jackson) simultaneously
@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = SimpleLinkSerializer.class)
@tools.jackson.databind.annotation.JsonSerialize(using = NewSimpleLinkSerializer.class)
public class Link { ... }
```
This requires both Jackson 2 and Jackson 3 jars on the classpath. Old serializers use `com.fasterxml` imports, new ones use `tools.jackson`.

### StdSerializer changes (Jackson 3)
```java
// Old (Jackson 2) - JsonObjectSerializer pattern
public class MySerializer extends JsonObjectSerializer<MyEntity> {
    @Override
    public void serializeObject(MyEntity value, JsonGenerator gen, SerializerProvider provider) { ... }
}

// New (Jackson 3) - StdSerializer with explicit writeStartObject/writeEndObject
public class MySerializer extends StdSerializer<MyEntity> {
    @Override
    public void serialize(MyEntity value, JsonGenerator gen, SerializerProvider provider) {
        gen.writeStartObject();
        // ... write fields ...
        gen.writeEndObject();
    }
}
```

### ObjectIdResolver changes (Jackson 3)
Override `bindItem()` to prevent duplicate ID binding errors:
```java
@Override
public void bindItem(ObjectIdGenerator.IdKey id, Object pojo) {
    if (items == null) {
        items = new HashMap<>();
    }
    Object existing = items.get(id);
    if (existing == null) {
        items.put(id, pojo);
    } else if (existing != pojo) {
        log.debug("Duplicate object id detected for id {}. Ignoring.", id.key);
    }
}
```

### CsvMapper migration (Jackson 3)
```java
// Old
CsvMapper csvMapper = new CsvMapper();
csvMapper.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);

// New
CsvMapper csvMapper = CsvMapper.builder()
    .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
    .build();
```

### JsonSchema module (Jackson 3)
```gradle
// Old
implementation "com.fasterxml.jackson.module:jackson-module-jsonSchema"

// New
implementation "tools.jackson.module:jackson-module-jsonSchema:3.1.1"
```

### @JsonDeserialize with builders
When migrating to Jackson 3, Lombok builders may need explicit annotation to respect default field values:
```java
@JsonDeserialize(builder = TypeSpec.TypeSpecBuilder.class)
public class TypeSpec { ... }
```

---

## 10. Spring Cloud / Netflix Migration

### Remove Netflix Ribbon and Hystrix
```gradle
// Remove these
implementation "org.springframework.cloud:spring-cloud-starter-netflix-ribbon"
implementation "org.springframework.cloud:spring-cloud-starter-netflix-hystrix"

// Add replacements
implementation "org.springframework.cloud:spring-cloud-starter-loadbalancer"
implementation "org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j"
```

### Migrate to Otel and configure there in gradle.build file
xm_commons_version must be at least 5.0.14
1. Remove if exists implementation "org.springframework.boot:spring-boot-micrometer-tracing-brave"
2. Add implementation "com.icthh.xm.commons:xm-commons-metric-lep:${xm_commons_version}"
3. Remove this bean if exists similiar
```java
   @Bean(name = "periodicMetricsTaskScheduler")
   public ThreadPoolTaskScheduler periodicMetricsTaskScheduler(ApplicationProperties applicationProperties) {
   ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
   taskScheduler.setPoolSize(applicationProperties.getPeriodicMetricPoolSize());
   taskScheduler.setThreadNamePrefix("pmetrics-executor-");
   taskScheduler.setWaitForTasksToCompleteOnShutdown(false);
   taskScheduler.setAwaitTerminationSeconds(1);
   taskScheduler.initialize();
   return taskScheduler;
   }
   ```
4. Remove all imports -> import com.codahale.* and replace 
 if you found import com.codahale.metrics.MetricRegistry; -> import io.micrometer.core.instrument.MeterRegistry; 
 and com.codahale.metrics.Gauge -> io.micrometer.core.instrument.Gauge

5. Change private final MetricRegistry metricRegistry; to MeterRegistry meterRegistry; and update this in tests also.
6. Update CustomMetricsConfiguration update method onRefresh  -> 
```java
   public void onRefresh(final String updatedKey, final String config) {
   try {
   String tenant = this.matcher.extractUriTemplateVariables(mappingPath, updatedKey).get("tenantName");
   String metricsNamePrefix = METRICS_PREFIX + tenant.toLowerCase();
   removeMetrics(metricsNamePrefix);

            if (isBlank(config)) {
                this.configuration.remove(tenant);
                periodMetricsService.scheduleCustomMetric(emptyList(), tenant);
                return;
            }

            List<CustomMetric> metrics = mapper.readValue(config, new TypeReference<>() {});
            this.configuration.put(tenant, metrics);

            log.info("Metric configuration was updated for tenant [{}] by key [{}]", tenant, updatedKey);
            registerMetrics(metrics, metricsNamePrefix, tenant);

            periodMetricsService.scheduleCustomMetric(metrics, tenant);
        } catch (Exception e) {
            log.error("Error read metric configuration from path {}", updatedKey, e);
        }
   }
```
7. Update method removeMetrics -> 
```java
private void removeMetrics(String metricsNamePrefix) {
        meterRegistry.getMeters().stream()
            .filter(meter -> meter.getId().getName().equals(metricsNamePrefix) ||
                          meter.getId().getName().startsWith(metricsNamePrefix + "."))
            .forEach(meterRegistry::remove);
    }
```
8. Update method or add registerMetrics →
```java
private void registerMetrics(List<CustomMetric> metrics, String metricsNamePrefix, String tenant) {
        metrics.forEach(metric -> {
            String gaugeName = metricsNamePrefix + "." + metric.getName();
            Gauge.builder(gaugeName, customMetricsService, cms -> toDouble(cms.getMetric(metric.getName(), metric, tenant)))
                 .description("Custom metric for tenant: " + tenant)
                 .register(meterRegistry);
        });
    }
```
9. Update method toDouble ->
```java
   private Double toDouble(Object value) {
   if (value instanceof Number num) {
   return num.doubleValue();
   }
   return Double.NaN;
   }
  ```
10. Add in PeriodicMetricsService constructor 
```java
public PeriodicMetricsService(CustomMetricsService customMetricsService, ThreadPoolTaskScheduler periodicMetricsTaskScheduler)
```
to
```java
public PeriodicMetricsService(CustomMetricsService customMetricsService, @Qualifier("periodicMetricsTaskScheduler") ThreadPoolTaskScheduler periodicMetricsTaskScheduler)
```
11. Remove all 12. Remove in gradle if exists 
```gradle
    implementation 'io.micrometer:micrometer-tracing'
    implementation 'io.micrometer:micrometer-core'
    implementation 'io.micrometer:micrometer-observation'
    implementation "io.micrometer:micrometer-tracing-bridge-brave"
```
13. Update/create application-tracing.yml
```yml
spring:
  kafka:
    template:
      observation-enabled: true # set kafka producer headers

management:
  tracing:
    propagation:
      type: w3c
    sampling:
      probability: 1.0 # report 100% of traces
    export:
      enabled: true
 ```
14. Update application.yml
```yml
management:
  opentelemetry:
    tracing:
      export:
        otlp:
          endpoint: http://localhost:4318/v1/traces
  otlp:
    metrics:
      export:
        enabled: false
 ```

### Remove Undertow explicit config
Remove custom Undertow HTTP/2 configuration and version constraints. Spring Boot 4 manages this via defaults.

---

## 11. Groovy / LEP Changes

### ASM imports changed
```java
// Old
import aj.org.objectweb.asm.Opcodes;

// New
import groovyjarjarasm.asm.Opcodes;
```
Use static imports for `ACC_PUBLIC`, `ACC_TRANSIENT`.

### LEP pre-compilation
Add Gradle task for LEP Groovy pre-compilation:
```gradle
tasks.register('preCompileLep', JavaExec) {
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'com.icthh.xm.commons.lep.groovy.LepCompiler'
    args = findProperty('exportArgs')?.split(' ')?.toList() ?: []
}
```

---

## 12. Test Migration

### Replace WireMock with OkHttp MockWebServer
```gradle
// Remove
testImplementation "org.wiremock:wiremock-standalone"

// Add
testImplementation "com.squareup.okhttp3:okhttp:4.12.0"
testImplementation "com.squareup.okhttp3:mockwebserver:4.12.0"
```

### Remove Cucumber
Remove all Cucumber test infrastructure (tasks, dependencies, runner classes):
```gradle
// Remove
testImplementation "io.cucumber:cucumber-java"
testImplementation "io.cucumber:cucumber-junit"
testImplementation "io.cucumber:cucumber-spring"
```

### @SpyBean -> @MockitoSpyBean
```java
// Old
import org.springframework.boot.test.mock.mockito.SpyBean;

// New
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
```

### spring-boot-starter-webmvc-test
```gradle
// Add for MVC tests
testImplementation "org.springframework.boot:spring-boot-starter-webmvc-test"
```

---

## 13. New Features Added

### JSON Schema Validation (xm-commons-data-spec)
New `JsonValidationUtils` utility using `com.networknt:json-schema-validator:3.0.1`:
```gradle
implementation("com.networknt:json-schema-validator:${versions.jsonSchemaValidatorVersion}")
```

### Streaming File Uploads
Support for streaming file uploads was added.

---

## 14. Removed / Deprecated

- `hibernateTypes60` / `hypersistence-utils` dependencies removed
- `grgit` and `sonarqube` removed from version management
- `configurePathMatch()` / `setUseSuffixPatternMatch()` in WebMvcConfigurer removed
- JUnit 4 replaced with JUnit 5
- `TenantAwareGrantRequestEntityConverter` replaced with `TenantAwareGrantRequestHeadersConverter`
- `jhipster` 8.x dependency updated to `jhipster-framework` 9.x
- Netflix Ribbon and Hystrix removed (replaced by LoadBalancer + Resilience4j)
- Undertow explicit version management removed
- Cucumber BDD test framework removed
- WireMock replaced with OkHttp MockWebServer
- Dropwizard metrics removed
- `providedRuntime` Gradle configuration removed

---

## Quick Migration Checklist

Each item below is self-contained — follow it without referencing other sections.

### Build & Java

1. [ ] **Java 25** — set `sourceCompatibility = JavaVersion.VERSION_25` and `targetCompatibility = JavaVersion.VERSION_25` in `build.gradle`. Update CI (`java-version: '25'`), Docker (`eclipse-temurin:25-jre`), Travis (`oraclejdk25`).

2. [ ] **Gradle 9.2.1** — in `gradle/wrapper/gradle-wrapper.properties` set `distributionUrl=https://services.gradle.org/distributions/gradle-9.2.1-bin.zip`, then run `./gradlew wrapper`.

3. [ ] **Gradle task syntax** — replace `task myTask(type: X) {}` with `tasks.register('myTask', X) {}`. Replace `destinationDir` → `destinationDirectory`, `reportOn` → `testResults.from()`, `rootProject.buildDir` → `rootProject.layout.buildDirectory`.

4. [ ] **JVM `--add-opens` for tests** — add to test block in `build.gradle`:
   ```gradle
   jvmArgs += [
       '--add-opens', 'java.base/java.lang=ALL-UNNAMED',
       '--add-opens', 'java.base/java.lang.reflect=ALL-UNNAMED',
       '--add-opens', 'java.base/java.util=ALL-UNNAMED'
   ]
   ```

5. [ ] **Remove custom Groovy version** — delete `groovy_version` from `gradle.properties` and any Groovy resolution strategy overrides. Spring Boot 4 manages Groovy internally.

6. [ ] **git-properties plugin** — set `gitPropertiesPluginVersion=2.5.4` in `gradle.properties`.

7. [ ] **Spring Boot plugin versions** — add explicit versions:
   ```gradle
   id "org.springframework.boot" version "${springBootVersion}"
   id "io.spring.dependency-management" version "${springDependencyManagement}"
   ```

8. [ ] **LEP pre-compilation task** — add to `build.gradle`:
   ```gradle
   tasks.register('preCompileLep', JavaExec) {
       classpath = sourceSets.main.runtimeClasspath
       mainClass = 'com.icthh.xm.commons.lep.groovy.LepCompiler'
       args = findProperty('exportArgs')?.split(' ')?.toList() ?: []
   }
   ```

### Spring Boot 3.3 → 4.0.4

9. [ ] **Spring Boot & Cloud versions** — set `springBootVersion=4.0.4`, `springCloudVersion=2025.1.0` in `gradle.properties`.

10. [ ] **KafkaProperties** — change import `o.s.boot.autoconfigure.kafka.KafkaProperties` → `o.s.boot.kafka.autoconfigure.KafkaProperties`. Change `buildConsumerProperties(null)` → `buildConsumerProperties()`. Add `implementation("org.springframework.boot:spring-boot-starter-kafka")`.

11. [ ] **RestTemplateBuilder** — change import to `o.s.boot.restclient.RestTemplateBuilder`. Rename `setConnectTimeout`→`connectTimeout`, `setReadTimeout`→`readTimeout`. **Builder is immutable** — capture result: `restTemplateBuilder = restTemplateBuilder.connectTimeout(d).readTimeout(d);`

12. [ ] **DefaultResponseErrorHandler** — old signature `handleError(ClientHttpResponse)` → new `handleError(URI url, HttpMethod method, ClientHttpResponse response)`.

13. [ ] **LiquibaseProperties** — change import `o.s.boot.autoconfigure.liquibase.LiquibaseProperties` → `o.s.boot.liquibase.autoconfigure.LiquibaseProperties`.

14. [ ] **WebMvcTest** — change import `o.s.boot.test.autoconfigure.web.servlet.WebMvcTest` → `o.s.boot.webmvc.test.autoconfigure.WebMvcTest`.

15. [ ] **MappingJackson2HttpMessageConverter** — replace with `JacksonJsonHttpMessageConverter` (import `o.s.http.converter.json.JacksonJsonHttpMessageConverter`).

16. [ ] **configureMessageConverters** — old: `void configureMessageConverters(List<HttpMessageConverter<?>>)` → new: `void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) { builder.configureMessageConvertersList(converters -> { ... }); }`

17. [ ] **AsyncConfiguration bean** — rename `@Bean(name = "taskExecutor")` → `@Bean(name = "applicationTaskExecutor")`. Update all `@Qualifier("taskExecutor")` references.

18. [ ] **ContentCachingRequestWrapper** — change `new ContentCachingRequestWrapper(request)` → `new ContentCachingRequestWrapper(request, 0)`.

19. [ ] **WebConfigurer** — remove `ServerProperties` dependency. Remove Undertow HTTP/2 configuration.

20. [ ] **HttpClient5/HttpCore5** — set `httpClient5Version=5.6.1`, `httpCore5Version=5.4.2`. Add `implementation "org.apache.httpcomponents.core5:httpcore5:${httpCore5Version}"` if needed.

21. [ ] **OAuth2 starters** — add if using OAuth2:
    ```gradle
    implementation "org.springframework.boot:spring-boot-starter-security-oauth2-client"
    implementation "org.springframework.boot:spring-boot-starter-security-oauth2-resource-server"
    ```

22. [ ] **Remove Tomcat exclusion** — remove `exclude module: 'spring-boot-starter-tomcat'` from `spring-boot-starter-web`.

23. [ ] **Remove `configurePathMatch`** — delete `configurePathMatch()` / `setUseSuffixPatternMatch()` overrides from WebMvcConfigurer implementations.

### Jackson 2 → 3

24. [ ] **Rename all imports** — `com.fasterxml.jackson.core.*` → `tools.jackson.core.*`, `com.fasterxml.jackson.databind.*` → `tools.jackson.databind.*`, `com.fasterxml.jackson.dataformat.yaml.*` → `tools.jackson.dataformat.yaml.*`. Note: `JsonFactory` moves to `tools.jackson.core.json.JsonFactory`.

25. [ ] **Maven/Gradle coordinates** — `com.fasterxml.jackson.*` group → `tools.jackson.*` group. Example: `tools.jackson.core:jackson-databind`, `tools.jackson.dataformat:jackson-dataformat-yaml`.

26. [ ] **ObjectMapper creation** — replace `new ObjectMapper()` → `JsonMapper.builder().build()`, `new ObjectMapper(new YAMLFactory())` → `YAMLMapper.builder().build()`. ObjectMapper is now immutable; use `.rebuild().build()` instead of `.copy()`.

27. [ ] **Jackson API renames** — `jsonNode.asText()` → `jsonNode.asString()`, `objectNode.fields()` → `objectNode.properties().iterator()`, `arrayNode.elements()` → `arrayNode.values()`, `TypeFactory.defaultInstance()` → `TypeFactory.createDefaultInstance()`.

28. [ ] **JsonParser** — `jsonFactory.createParser(json)` → `jsonFactory.createParser(ObjectReadContext.empty(), json)`. `jParser.getCurrentName()` → `jParser.currentName()`.

29. [ ] **Exception handling** — `JacksonException` is now `RuntimeException` (unchecked). Replace `catch (IOException)` / `catch (JsonProcessingException)` around Jackson ops with `catch (JacksonException)`.

30. [ ] **Remove JavaTimeModule** — delete `import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule` and `objectMapper.registerModule(new JavaTimeModule())`. Jackson 3 has built-in Java 8 date/time support.

31. [ ] **SerializationFeature.INDENT_OUTPUT** — no longer exists as a feature. Use `objectMapper.writerWithDefaultPrettyPrinter()` instead of `objectMapper.enable(SerializationFeature.INDENT_OUTPUT)`.

32. [ ] **Custom serializers** — migrate from `JsonObjectSerializer<T>` to `StdSerializer<T>`. In `serialize()` method, manually call `gen.writeStartObject()` / `gen.writeEndObject()`.

33. [ ] **ObjectIdResolver** — add `bindItem()` override to prevent duplicate ID errors:
    ```java
    @Override
    public void bindItem(ObjectIdGenerator.IdKey id, Object pojo) {
        if (items == null) items = new HashMap<>();
        Object existing = items.get(id);
        if (existing == null) items.put(id, pojo);
    }
    ```

34. [ ] **Dual Jackson annotations** — if domain classes are serialized by both Jackson 2 and Jackson 3 ObjectMappers (e.g., commons lib used by old consumers), add BOTH annotations:
    ```java
    @com.fasterxml.jackson.annotation.JsonIgnore
    @tools.jackson.annotation.JsonIgnore
    ```
    Jackson 2 annotations are invisible to Jackson 3 ObjectMapper and vice versa.

35. [ ] **jackson-datatype-hibernate7** — change `com.fasterxml.jackson.datatype:jackson-datatype-hibernate6` → `tools.jackson.datatype:jackson-datatype-hibernate7`.

36. [ ] **ObjectMapper bean customization** — Spring Boot 4 pattern:
    ```java
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public JsonMapper objectMapper(List<JsonMapperBuilderCustomizer> customizers) {
        JsonMapper.Builder builder = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION);
        customizers.forEach(c -> c.customize(builder));
        return builder.build();
    }
    ```
36. [ ] Change from old -> 
```java
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.setVisibility(PropertyAccessor.ALL, NONE);
objectMapper.setVisibility(PropertyAccessor.FIELD, ANY);

YAMLMapper.builder()
.changeDefaultVisibility(checker -> checker
.withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
).build(); 
```

37. [ ] **CsvMapper** — replace `new CsvMapper()` + `csvMapper.disable(...)` with `CsvMapper.builder().disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY).build()`.

38. [ ] **JsonSchema module** — change `com.fasterxml.jackson.module:jackson-module-jsonSchema` → `tools.jackson.module:jackson-module-jsonSchema:3.1.1`.

39. [ ] **Validation message path format** — networknt 3.x changed error paths from `$.fieldName` to `/fieldName`. Update tests and logic that parse validation error paths.

### Hibernate 6.5 → 7.3.1.Final

40. [ ] **Hibernate version** — set `hibernate: '7.3.1.Final'` in version map.

41. [ ] **Remove hibernate-types** — delete `hibernateTypes60` (vladmihalcea) and `io.hypersistence:hypersistence-utils-hibernate-63` dependencies. Not compatible with Hibernate 7.

42. [ ] **JPA Metamodel** — if you have custom Metamodel implementations (e.g., in tests), add `entity(String s)` method, change collection methods to return `Set.of()` instead of `null`.

### OAuth2 Client

43. [ ] **Token response client** — replace `DefaultClientCredentialsTokenResponseClient` → `RestClientClientCredentialsTokenResponseClient`. Replace `setRequestEntityConverter(converter)` → `addHeadersConverter(converter)`.

44. [ ] **Converter pattern** — change `implements Converter<OAuth2ClientCredentialsGrantRequest, RequestEntity<?>>` → `implements Converter<OAuth2ClientCredentialsGrantRequest, HttpHeaders>`. Update imports from `DefaultClientCredentialsTokenResponseClient` / `OAuth2ClientCredentialsGrantRequestEntityConverter` to `RestClientClientCredentialsTokenResponseClient` / `DefaultOAuth2TokenRequestHeadersConverter`.

### JUnit 4 → 5

45. [ ] **Test annotations** — `@RunWith(SpringRunner.class)` → `@ExtendWith(SpringExtension.class)`, `@Before` → `@BeforeEach`, `@After` → `@AfterEach`, `@Test` (org.junit) → `@Test` (org.junit.jupiter.api).

46. [ ] **Mock annotations** — `@MockBean` (o.s.boot.test.mock.mockito) → `@MockitoBean` (o.s.test.context.bean.override.mockito), `@SpyBean` → `@MockitoSpyBean`.

47. [ ] **Gradle test config** — add `test { useJUnitPlatform() }`. Add `testImplementation('org.junit.jupiter:junit-jupiter')`, `testRuntimeOnly('org.junit.platform:junit-platform-launcher')`. Remove `testImplementation('junit:junit')`.

48. [ ] **MockitoAnnotations** — replace `MockitoAnnotations.initMocks(this)` → `MockitoAnnotations.openMocks(this)` or use `@ExtendWith(MockitoExtension.class)`.

### Spring Data JPA

49. [ ] **Specification.where(null)** — replace with `Specification.where(alwaysTrue())` where `private static <T> Specification<T> alwaysTrue() { return (root, query, cb) -> cb.conjunction(); }`.

### Other Dependencies & Cleanup

50. [ ] **Spring Cloud Netflix** — remove `spring-cloud-starter-netflix-ribbon` and `spring-cloud-starter-netflix-hystrix`. Add `spring-cloud-starter-loadbalancer` and `spring-cloud-starter-circuitbreaker-resilience4j`.

51. [ ] **JHipster** — change `jhipsterDependenciesVersion=8.6.0` → `jhipsterFrameworkDependenciesVersion=9.0.0`.

52. [ ] **networknt JSON Schema Validator 1.5→3.0** — `JsonSchemaFactory`→`SchemaRegistry`, `JsonSchema`→`Schema`, `ValidationMessage`→`Error`, `factory.getSchema()`→`registry.getSchema()`. Use `SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)`.

53. [ ] **ASM imports for Groovy** — change `import aj.org.objectweb.asm.Opcodes` → `import groovyjarjarasm.asm.Opcodes`.

54. [ ] **Remove Cucumber** — delete Cucumber dependencies (`io.cucumber:cucumber-java/junit/spring`), runner classes, feature files, and Gradle Cucumber tasks.

55. [ ] **Replace WireMock with MockWebServer** — if used, remove `org.wiremock:wiremock-standalone`, add `com.squareup.okhttp3:okhttp:4.12.0` and `com.squareup.okhttp3:mockwebserver:4.12.0`.

56. [ ] **PMD 6→7** — PMD 7 has breaking rule config changes. Review and update PMD config files, or temporarily disable PMD tasks.

57. [ ] **Dependency versions** — update: JaCoCo `0.8.14`, Checkstyle `13.4.0`, SpotBugs `4.9.8.3` (plugin `6.4.8`), PMD `7.23.0`, EqualsVerifier `3.19.1`, Commons Codec `1.17.2`, Commons IO `2.21.0`, Commons Collections4 `4.5.0`, Commons Text `1.13.0`, Guava `33.4.0-jre`, Lombok `1.18.42`, Logstash Logback Encoder `8.0`, Caffeine `3.2.0`, Swagger Codegen `3.0.68`, Hibernate Validation `8.0.2.Final`, MapStruct `1.6.3`, Lucene `10.4.0`, JSON Schema Validator `3.0.2`.

58. [ ] **Remove deprecated** — remove `providedRuntime` Gradle configuration, remove Undertow explicit version constraints, remove Dropwizard metrics dependencies.
61. Remove all exclude module: "spring-boot-starter-tomcat" - we must use tomcat!
### Add async appender and other
3. replace logback-spring.xml add default async appender as 
```xml
<appender name="STDOUT_SYNC" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <charset>utf-8</charset>
        <Pattern>%d %-5level [%thread] [${springAppName:-}] [%X{traceId:-}] [%X{rid}] %logger{0}: %msg%n</Pattern>
    </encoder>
</appender>

<appender name="STDOUT_JSON_SYNC" class="ch.qos.logback.core.ConsoleAppender">
<encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>

<appender name="STDOUT" class="ch.qos.logback.classic.AsyncAppender">
<queueSize>512</queueSize>
<appender-ref ref="STDOUT_SYNC"/>
</appender>

<appender name="STDOUT_JSON" class="ch.qos.logback.classic.AsyncAppender">
<queueSize>512</queueSize>
<appender-ref ref="STDOUT_JSON_SYNC"/>
</appender>
```
5. Add in tracing profile 
```yaml
  opentelemetry:
    tracing:
      export:
        otlp:
          endpoint: ${OTEL_EXPORTER_OTLP_TRACES_ENDPOINT:http://localhost:4317}
  otlp:
    metrics:
      export:
        url: ${OTEL_EXPORTER_OTLP_HTTP_BASE_URL:http://localhost:4318}/v1/metrics
```
6. Add in sonar.gradle 
```groovy
test.finalizedBy jacocoTestReport
jacocoTestReport.dependsOn test
```
7. Update version java to 25 in all workflows .github
```yml
java-version: 25
```
8. Add in logback 
```xml
<logger name="java.lang.ProcessBuilder" level="INFO"/>
<logger name="io.opentelemetry.sdk.trace" level="WARN"/>
```
9. First, Check if exists file [application-psql.yml] IF Not SKIP! create [bootstrap-psql.yml](../src/main/resources/config/bootstrap-psql.yml) and same with [bootstrap-dev.yml](../src/main/resources/config/bootstrap-dev.yml)
```yaml
spring:
    docker:
        compose:
            enabled: true
            file: src/main/docker/postgresql.yml
            lifecycle-management: start_and_stop
            stop:
                command: down
```
10. !Update IF EXIST file IF NOT, SKIP IT! [application-psql.yml](../src/main/resources/config/application-psql.yml) and application-dev
```yaml
url: jdbc:postgresql://localhost:${POSTGRES_PORT:5432}/(used db in application-dev.properties)
```
11. Update (remove if exist) implementation "com.h2database:h2" [profile_dev.gradle](../gradle/profile_dev.gradle)
```groovy
dependencies {
     
    implementation "org.postgresql:postgresql"
    testImplementation "org.testcontainers:postgresql"
    developmentOnly "org.springframework.boot:spring-boot-docker-compose"
}
bootRun {
    args = ["--spring.profiles.active=${springProfiles}"]
    systemProperty 'user.timezone', 'UTC'
    environment 'POSTGRES_PORT', '0'
}
```
12. Check application-dev.properties and datasource: url: and check what name database is use. And then, update [postgresql.yml](../src/main/docker/postgresql.yml) update image, add new env veriable POSTGRES_DB, update port
```yml
  image: postgres:18.4
  environment:
    - POSTGRES_DB=(used db in application-dev.properties)
  ports:
    - "127.0.0.1:${POSTGRES_PORT}:5432"
```
13. Off tracing otlp in [application-dev.yml](../src/main/resources/config/application-dev.yml) and also in test module in application.yml!
```yml
management:
  tracing:
    export:
      enabled: false
  otlp:
    metrics:
      export:
        enabled: false
```
14. Change in [.travis.yml](.travis.yml) 
```yml
language: java
jdk:
  - openjdk25
```
15. Update from `image = "eclipse-temurin:21-jre-alpine"` to `image = "eclipse-temurin:25-jre"`
16. Update Dockerfile `FROM eclipse-temurin:21-jre` to `FROM eclipse-temurin:25-jre`
17. in build old `org.springframework.boot:spring-boot-starter-aop` change to `org.springframework.boot:spring-boot-starter-aspectj`
### Test refactored 
1. All test which contains or started from @Test(expected need to - 
remove this (expected.. and add in test check via assertThrows(expected Exception.class, () -> and call method witch throw exception))
2. Find in application.yml file keys as change there to 
```yml
  naming:
   physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
   implicit-strategy: org.springframework.boot.hibernate.SpringImplicitNamingStrategy
```



