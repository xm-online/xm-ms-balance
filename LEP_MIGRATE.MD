# LEP Engine Migration Guide - Complete Step-by-Step Instructions

## Overview
This guide provides comprehensive, detailed steps to migrate any XM microservice from the old LEP engine to the new LEP engine version (xm-commons 2.3.x+). Follow these steps in order for a successful migration.

## Table of Contents
1. [Dependencies Update](#1-dependencies-update)
2. [LEP Configuration Migration](#2-lep-configuration-migration)
3. [LEP Context Refactoring](#3-lep-context-refactoring)
4. [LEP Context Factory Implementation](#4-lep-context-factory-implementation)
5. [LEP Key Resolvers Migration](#5-lep-key-resolvers-migration)
6. [@LepService Interfaces Migration](#6-lepservice-interfaces-migration)
7. [Thread Context Management Update](#7-thread-context-management-update)
8. [WebMvcConfiguration Update (Optional)](#8-webmvcconfiguration-update-optional)
9. [Application Properties (Optional)](#9-application-properties-optional)
10. [Test Configuration Migration](#10-test-configuration-migration)
11. [CoreContextsHolder Removal from LEPs](#11-corecontextsholder-removal-from-leps)
12. [Verification Steps](#12-verification-steps)

---

## 1. Dependencies Update

### Location: `build.gradle`

**Action:** Add new LEP dependencies

**Add these lines to the dependencies block:**
```groovy
implementation "com.icthh.xm.commons:xm-commons-lep-groovy:${xm_commons_version}"
implementation "com.icthh.xm.commons:xm-commons-lep-js:${xm_commons_version}" // Optional, add if JS support needed
```

### Location: `gradle.properties`

**Action:** Update xm_commons_version

**Before:**
```properties
xm_commons_version=2.2.x
```

**After:**
```properties
xm_commons_version=2.3.37
```
**Note:** Use version 2.3.37 or higher. Check the latest version from xm-commons releases.

**Also increment your microservice version:**
```properties
version=x.x.x
```

---

## 2. LEP Configuration Migration

### Location: `src/main/java/.../config/LepConfiguration.java`

**Action:** Refactor LepConfiguration class

**Before (Old Pattern):**
```java
@Configuration
@EnableLepServices(basePackageClasses = ApplicationStartup.class)
public class LepConfiguration extends WebLepSpringConfiguration {

    private final ApplicationEventPublisher eventPublisher;

    public LepConfiguration(
        @Value("${spring.application.name}") String appName,
        ApplicationEventPublisher eventPublisher,
        ResourceLoader resourceLoader) {
        super(appName);
        this.eventPublisher = eventPublisher;
    }

    // ... other methods
}
```

**After (New Pattern):**
```java
@Configuration
public class LepConfiguration extends GroovyLepEngineConfiguration {

    public LepConfiguration(@Value("${spring.application.name}") String appName) {
        super(appName);
    }
}
```

**Key Changes:**
1. Remove `@EnableLepServices` annotation completely
2. Change parent class from `WebLepSpringConfiguration` to `GroovyLepEngineConfiguration`
3. Simplify constructor to accept only `appName` parameter
4. Remove `ApplicationEventPublisher` and `ResourceLoader` parameters
5. Remove any web-specific configuration methods

**Important:** If your LepConfiguration doesn't have any custom methods, you can keep it minimal as shown above. The base class `GroovyLepEngineConfiguration` provides all necessary configuration.

---

## 3. LEP Context Refactoring

### Location: `src/main/java/.../lep/LepContext.java`

**Action:** Refactor LepContext to extend BaseLepContext

**Before (Old Pattern):**
```java
public class LepContext {
    public Commons commons;
    public InArgs inArgs;
    public Lep lep;
    public LepThreadHelper thread;
    public AuthContext authContext;

    public LepServices services;
    public LepTemplates templates;

    public static class LepServices {
        private BalanceService balanceService;
        private PocketService pocketService;
    }

    public static class LepTemplates {
        private RestTemplate rest;
    }
}
```

**After (New Pattern):**
```java
public class LepContext extends BaseLepContext implements TraceServiceField, OutboxTransportServiceField {

    public LepServices services;
    public LepTemplates templates;

    public static class LepServices {
        public BalanceService balanceService;
        public PocketService pocketService;
        public BalanceHistoryService balanceHistoryService;
        public MetricService metricService;
        public TenantConfigService tenantConfigService;
        public PocketQueryService pocketQueryService;
    }

    public static class LepTemplates {
        public RestTemplate rest;
    }
}
```

**Key Changes:**
1. **Extend `BaseLepContext`** - This provides commons, inArgs, lep, thread, authContext fields automatically
2. **Implement marker interfaces:**
   - `TraceServiceField` - Adds traceService to context (from xm-commons)
   - `OutboxTransportServiceField` - Adds outboxTransportService to context (from xm-commons)
3. **Remove explicit field declarations** for: commons, inArgs, lep, thread, authContext (now inherited from BaseLepContext)
4. **Change field visibility to public** in nested classes (from private to public)
5. **Keep only microservice-specific fields:** services and templates

**Convention:** Class MUST be named `LepContext` - this is a naming convention!

**Marker Interfaces Explanation:**
- Marker interfaces like `TraceServiceField` and `OutboxTransportServiceField` are provided by xm-commons libraries
- They provide type-safe access to globally available services in LEP context
- The actual service instances are automatically injected by the framework
- You don't need to implement any methods - they're all default methods in the interfaces

---

## 4. LEP Context Factory Implementation

### Step 1: Preserve LepContext Structure from Old Listener

**⚠️ CRITICAL: Before deleting the old listener, you MUST preserve the LEP context structure!**

**Find the old listener class that extends `SpringLepProcessingApplicationListener`:**
- Look for files named like `*LepProcessingApplicationListener.java`
- Examples:
  - `XmEntityMsLepProcessingApplicationListener.java`
  - `UaaLepProcessingApplicationListener.java`
  - `BalanceLepProcessingApplicationListener.java`
  - `XmActivationLepProcessingApplicationListener.java`
- **The class name varies by microservice, but it ALWAYS extends `SpringLepProcessingApplicationListener`**

**Examine the old listener structure:**
```java
@Component
public class BalanceLepProcessingApplicationListener extends SpringLepProcessingApplicationListener {

    private final BalanceService balanceService;
    private final PocketService pocketService;
    private final RestTemplate restTemplate;
    private final TenantConfigService tenantConfigService;

    // Constructor with all dependencies
    public BalanceLepProcessingApplicationListener(
        BalanceService balanceService,
        PocketService pocketService,
        @Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate,
        TenantConfigService tenantConfigService) {
        this.balanceService = balanceService;
        this.pocketService = pocketService;
        this.restTemplate = restTemplate;
        this.tenantConfigService = tenantConfigService;
    }

    @Override
    protected void processBeforeRefresh(ApplicationEvent applicationEvent,
                                       LepMangerService mangerService) {
        mangerService.beginThreadContext(ctx -> {
            BalanceLepContext lepContext = new BalanceLepContext();

            // ⭐ THIS STRUCTURE IS WHAT YOU NEED TO PRESERVE!
            lepContext.services = new BalanceLepContext.LepServices();
            lepContext.services.balanceService = balanceService;
            lepContext.services.pocketService = pocketService;
            lepContext.services.tenantConfigService = tenantConfigService;

            lepContext.templates = new BalanceLepContext.LepTemplates();
            lepContext.templates.rest = restTemplate;

            ctx.setValue("lepContext", lepContext);
        });
    }
}
```

**Action Plan:**
1. **If `LepContext.java` already exists** - Verify it matches the structure from the old listener's processBeforeRefresh method
2. **If `LepContext.java` DOES NOT exist** - Create it using the exact structure from the old listener
3. Extract all service assignments from processBeforeRefresh
4. Extract all template assignments from processBeforeRefresh
5. Note all field names and their types
6. Keep the exact same nested class structure (LepServices, LepTemplates, etc.)
7. **Preserve custom field names** - if the old code had typos or unusual names, keep them for backward compatibility

**Example - Creating LepContext from old listener:**

If your old listener had this structure in processBeforeRefresh:
```java
lepContext.services = new BalanceLepContext.LepServices();
lepContext.services.balanceService = balanceService;
lepContext.services.pocketService = pocketService;
lepContext.services.balanceHistoryService = balanceHistoryService;
lepContext.services.metricService = metricService;

lepContext.templates = new BalanceLepContext.LepTemplates();
lepContext.templates.rest = restTemplate;
lepContext.templates.kafka = kafkaTemplate;
```

Then your new `LepContext.java` should have **exactly** this structure:
```java
public class LepContext extends BaseLepContext implements TraceServiceField {

    public LepServices services;
    public LepTemplates templates;

    public static class LepServices {
        public BalanceService balanceService;              // ← from listener
        public PocketService pocketService;                // ← from listener
        public BalanceHistoryService balanceHistoryService; // ← from listener
        public MetricService metricService;                // ← from listener
    }

    public static class LepTemplates {
        public RestTemplate rest;      // ← from listener
        public KafkaTemplate kafka;    // ← from listener
    }
}
```

**🔴 KEY RULE: The structure in the new LepContext MUST exactly match the structure that was being created in the old listener's processBeforeRefresh method! Same field names, same types, same nested structure!**

### Step 2: Create New Context Factory (Using Preserved Structure)

**Location:** Create new file `src/main/java/.../lep/LepContextFactory.java` (or similar name like `BalanceLepContextFactory.java`)

**Action:** Create a factory that replicates the exact same context initialization logic from the old listener

**Implementation Pattern:**
```java
@Component
public class BalanceLepContextFactory implements LepContextFactory {

    // ⭐ Inject the SAME services that were in the old listener
    private final BalanceService balanceService;
    private final PocketService pocketService;
    private final PocketQueryService pocketQueryService;
    private final BalanceHistoryService balanceHistoryService;
    private final MetricService metricService;
    private final RestTemplate restTemplate;
    private final TenantConfigService tenantConfigService;

    // ⭐ Use the SAME constructor parameters as the old listener
    public BalanceLepContextFactory(
            BalanceService balanceService,
            PocketService pocketService,
            PocketQueryService pocketQueryService,
            BalanceHistoryService balanceHistoryService,
            MetricService metricService,
            @Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate,
            TenantConfigService tenantConfigService) {
        this.balanceService = balanceService;
        this.pocketService = pocketService;
        this.pocketQueryService = pocketQueryService;
        this.balanceHistoryService = balanceHistoryService;
        this.metricService = metricService;
        this.restTemplate = restTemplate;
        this.tenantConfigService = tenantConfigService;
    }

    @Override
    public BaseLepContext buildLepContext(LepMethod lepMethod) {
        LepContext lepContext = new LepContext();

        // ⭐ Replicate the EXACT SAME initialization from old listener
        lepContext.services = new LepContext.LepServices();
        lepContext.services.balanceService = balanceService;
        lepContext.services.pocketService = pocketService;
        lepContext.services.pocketQueryService = pocketQueryService;
        lepContext.services.balanceHistoryService = balanceHistoryService;
        lepContext.services.metricService = metricService;
        lepContext.services.tenantConfigService = tenantConfigService;

        lepContext.templates = new LepContext.LepTemplates();
        lepContext.templates.rest = restTemplate;

        return lepContext;
    }
}
```

**Migration Mapping:**
```
Old Listener                              New Factory
─────────────────────────────────────────────────────────────────────
class extends SpringLepProcessing...  →  class implements LepContextFactory

Field injections in listener          →  Same field injections in factory

processBeforeRefresh(...)             →  buildLepContext(LepMethod lepMethod)

lepContext = new BalanceLepContext()  →  lepContext = new LepContext()

lepContext.services = new ...         →  Same: lepContext.services = new ...
lepContext.services.xxx = yyy         →  Same: lepContext.services.xxx = yyy

ctx.setValue("lepContext", lepContext) →  return lepContext
```

**Key Implementation Details:**
1. **@Component annotation** - Makes it a Spring bean
2. **Implements `LepContextFactory` interface** - Required by new LEP engine
3. **Constructor injection** - Inject the SAME services that were in the old listener
4. **Use @Qualifier** when needed (e.g., for specific RestTemplate beans like "loadBalancedRestTemplate")
5. **buildLepContext method:**
   - Create new `LepContext()` instance
   - Initialize nested objects in the SAME ORDER as old listener
   - Assign all service references exactly as before
   - Return the populated context

### Step 3: Delete Old Listener (Only After Verification)

**Files to delete (after creating factory):**
- `XmEntityMsLepProcessingApplicationListener.java`
- `SpringLepProcessingApplicationListener.java`
- `XmUaaLepProcessingApplicationListener.java`
- `XmActivationLepProcessingApplicationListener.java`
- Or whatever your listener class was named

**⚠️ DO NOT delete until you have:**
1. ✅ Created LepContext with preserved structure
2. ✅ Created LepContextFactory with same initialization logic
3. ✅ Verified all services are injected in the factory
4. ✅ Verified buildLepContext replicates the old processBeforeRefresh logic

---

## 5. LEP Key Resolvers Migration

### Pattern 1: Simple AppendLepKeyResolver Migration

**Before (Old Pattern):**
```java
@Component
public class ChangeStateTransitionLepKeyResolver extends AppendLepKeyResolver {

    @Override
    protected String[] getAppendSegments(SeparatorSegmentedLepKey baseKey,
                                         LepMethod method,
                                         LepManagerService managerService) {
        String translatedXmEntityTypeKey = translateToLepConvention(getRequiredStrParam(method, "xmEntityTypeKey"));
        String translatedPrevStateKey = translateToLepConvention(getRequiredStrParam(method, "prevStateKey"));
        String translatedNextStateKey = translateToLepConvention(getRequiredStrParam(method, "nextStateKey"));

        return new String[] {
            translatedXmEntityTypeKey,
            translatedPrevStateKey,
            translatedNextStateKey
        };
    }
}
```

**After (New Pattern):**
```java
@Component
public class ChangeStateTransitionLepKeyResolver implements LepKeyResolver {

    @Override
    public List<String> segments(LepMethod method) {
        return List.of(
                method.getParameter("xmEntityTypeKey", String.class),
                method.getParameter("prevStateKey", String.class),
                method.getParameter("nextStateKey", String.class)
        );
    }
}
```

**Key Changes:**
1. Change from `extends AppendLepKeyResolver` to `implements LepKeyResolver`
2. Replace `getAppendSegments()` method with `segments()` method
3. Use `method.getParameter(paramName, type)` instead of `getRequiredStrParam(method, paramName)`
4. Return `List<String>` instead of `String[]`
5. Remove `translateToLepConvention()` - new engine handles convention automatically
6. Simplified method signature - no baseKey, no managerService parameters

### Pattern 2: Advanced Resolver with Custom Group

**Before (Old Pattern):**
```java
@Component
public class CommonsLepResolver extends SeparatorSegmentedLepKeyResolver {
    @Override
    protected LepKey resolveKey(SeparatorSegmentedLepKey inBaseKey, LepMethod method, LepManagerService managerService) {
        String group = getRequiredParam(method, "group", String.class) + ".Commons";
        String name = getRequiredParam(method, "name", String.class);
        SeparatorSegmentedLepKey baseKey = new SeparatorSegmentedLepKey(group, XmLepConstants.EXTENSION_KEY_SEPARATOR, XmLepConstants.EXTENSION_KEY_GROUP_MODE);
        GroupMode groupMode = new GroupMode.Builder().prefixAndIdIncludeGroup(baseKey.getGroupSegmentsSize()).build();
        return baseKey.append(name, groupMode);
    }
}
```

**After (New Pattern):**
```java
@Component
public class CommonsLepResolver implements LepKeyResolver {

    @Override
    public String group(LepMethod method) {
        return method.getParameter("group", String.class);
    }

    @Override
    public List<String> segments(LepMethod method) {
        return List.of(method.getParameter("name", String.class));
    }
}
```

**Key Changes:**
1. Change from `extends SeparatorSegmentedLepKeyResolver` to `implements LepKeyResolver`
2. Replace `resolveKey()` with two methods: `group()` and `segments()`
3. **group()** - Returns the group name (without ".Commons" suffix if not needed)
4. **segments()** - Returns the list of key segments
5. Remove complex `SeparatorSegmentedLepKey` and `GroupMode` logic
6. Use simple `method.getParameter()` API

### Pattern 3: GroupLepKeyResolver Migration

**Before (Old Pattern):**
```java
@Component
public abstract class GroupLepKeyResolver extends SeparatorSegmentedLepKeyResolver {

    @Override
    protected LepKey resolveKey(SeparatorSegmentedLepKey baseKey,
                                LepMethod method,
                                LepManagerService managerService) {
        String group = getBaseGroup(baseKey) + "." + getAdditionalGroup(method);
        // ... complex logic
    }

    protected abstract String[] getAppendSegments(LepMethod method);
}
```

**After (New Pattern):**
```java
@Component
public abstract class GroupLepKeyResolver implements LepKeyResolver {

    @Override
    public String group(LepMethod method) {
        // Use dot notation for group hierarchy
        return "baseGroup.subGroup";
    }

    @Override
    public abstract List<String> segments(LepMethod method);
}
```

**Key Changes:**
1. Use `implements LepKeyResolver`
2. Implement `group()` method with dot notation (e.g., "baseGroup.subGroup")
3. Keep `segments()` as abstract method for subclasses to implement
4. Removed separator-based grouping logic

---

## 6. @LepService Interfaces Migration

**Action:** Convert interfaces annotated with @LepService to classes

**Before (Old Pattern):**
```java
@LepService(group = "service")
public interface CustomService {

    @LogicExtensionPoint("CustomFunction")
    String customFunction(String param);
}
```

**After (New Pattern):**
```java
@Service
@LepService(group = "service")
public class CustomService {

    @LogicExtensionPoint("CustomFunction")
    public String customFunction(String param) {
        throw new UnsupportedOperationException("This method should be implemented in LEP");
        // Alternative: provide default implementation
        // return "default value";
    }
}
```

**Key Changes:**
1. Change `interface` to `class`
2. Add `@Service` annotation (or `@Component`)
3. Make methods throw `UnsupportedOperationException` or provide default implementation
4. Keep `@LepService` and `@LogicExtensionPoint` annotations

**Rationale:** The new LEP engine requires actual classes instead of interfaces for proper proxy creation and method interception.

---

## 7. Thread Context Management Update

**Action:** Replace old lepManager thread context methods with new lepManagementService

**Before (Old Pattern):**
```java
lepManager.beginThreadContext(threadContext -> {
    threadContext.setValue(THREAD_CONTEXT_KEY_TENANT_CONTEXT, tenantContextHolder.getContext());
    threadContext.setValue(THREAD_CONTEXT_KEY_AUTH_CONTEXT, authContextHolder.getContext());
});

try {
    // run lep methods
} finally {
    lepManager.endThreadContext();
}
```

**After (New Pattern):**
```java
try (var context = lepManagementService.beginThreadContext()) {
    // run lep methods
}
```

**Key Changes:**
1. Use `lepManagementService` instead of `lepManager`
2. Use try-with-resources statement
3. No need to manually set tenant and auth context - handled automatically by the framework
4. No need to call `endThreadContext()` - automatically cleaned up when try block exits

**Alternative without try-with-resources:**
```java
lepManagementService.beginThreadContext();
try {
    // run lep methods
} finally {
    lepManagementService.endThreadContext();
}
```

**Important:** If you need to set tenant context before beginThreadContext, use:
```java
TenantContextUtils.setTenant(tenantContextHolder, "TENANT_NAME");
try (var context = lepManagementService.beginThreadContext()) {
    // run lep methods
}
```

---

## 8. WebMvcConfiguration Update (Optional)

### Location: `src/main/java/.../config/WebMvcConfiguration.java`

**Action:** Update to extend XmWebMvcConfigurerAdapter

**Before (Old Pattern):**
```java
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .excludePathPatterns("/error");
    }
}
```

**After (New Pattern):**
```java
@Configuration
public class WebMvcConfiguration extends XmWebMvcConfigurerAdapter {

    private final TenantInterceptor tenantInterceptor;
    private final XmLoggingInterceptor xmLoggingInterceptor;

    public WebMvcConfiguration(TenantInterceptor tenantInterceptor,
                              XmLoggingInterceptor xmLoggingInterceptor) {
        this.tenantInterceptor = tenantInterceptor;
        this.xmLoggingInterceptor = xmLoggingInterceptor;
    }

    @Override
    protected void xmAddInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .excludePathPatterns(getTenantIgnorePathPatterns());
        registry.addInterceptor(xmLoggingInterceptor);
    }

    @Override
    protected List<String> getTenantIgnorePathPatterns() {
        return List.of("/error", "/management/health/**");
    }
}
```

**Key Changes:**
1. Change from `implements WebMvcConfigurer` to `extends XmWebMvcConfigurerAdapter`
2. Inject interceptors via constructor
3. Use `xmAddInterceptors()` instead of `addInterceptors()`
4. Implement `getTenantIgnorePathPatterns()` to define excluded paths
5. Add XmLoggingInterceptor if not already present

**Note:** This step is optional but recommended for consistency across microservices.

---

## 9. Application Properties (Optional)

### Location: `src/main/resources/application.yml` or `application.properties`

**Action:** Add LEP-related configuration properties

**Add these properties (optional but recommended):**
```yaml
application:
  lep:
    full-recompile-on-lep-update: false  # Set to true to force full recompilation
    warmup-scripts: true                  # Enable script warmup on startup
    tenants-with-lep-warmup:             # List of tenants to warmup
      - TENANT1
      - TENANT2
```

**Or in properties format:**
```properties
application.lep.full-recompile-on-lep-update=false
application.lep.warmup-scripts=true
application.lep.tenants-with-lep-warmup=TENANT1,TENANT2
```

**Corresponding Java Configuration Class Update:**

**Location:** `src/main/java/.../config/ApplicationProperties.java`

```java
public class ApplicationProperties {

    private Lep lep = new Lep();

    public static class Lep {
        private Boolean fullRecompileOnLepUpdate = false;
        private Boolean warmupScripts = false;
        private List<String> tenantsWithLepWarmup = new ArrayList<>();

        // getters and setters
    }

    // getters and setters
}
```

**Explanation:**
- `fullRecompileOnLepUpdate`: Forces complete script recompilation when LEP is updated (slower but safer)
- `warmupScripts`: Precompiles scripts on application startup to improve first-run performance
- `tenantsWithLepWarmup`: List of tenant keys to warmup scripts for

---

## 10. Test Configuration Migration

### Step 1: Create or Update Test LEP Configuration

**Location:** `src/test/java/.../config/TestLepConfiguration.java`

**Implementation:**
```java
@Configuration
public class TestLepConfiguration extends GroovyLepEngineConfiguration {

    public TestLepConfiguration(@Value("${spring.application.name}") String appName) {
        super(appName);
    }

    @Override
    public LepUpdateMode lepUpdateMode() {
        return LepUpdateMode.SYNCHRONOUS;
    }

    @Bean
    public LoggingConfigService loggingConfigService() {
        return new LoggingConfigServiceStub();
    }

    @Bean
    public TenantAliasService tenantAliasService() {
        return new TenantAliasService(
            mock(CommonConfigRepository.class),
            mock(TenantListRepository.class)
        );
    }
}
```

**Key Points:**
1. Extend `GroovyLepEngineConfiguration`
2. Override `lepUpdateMode()` to return `LepUpdateMode.SYNCHRONOUS` (important for tests!)
3. Provide stub implementations for logging and tenant services
4. Use mocks for repository dependencies

### Step 2: Update Test Class Annotations

**Before (Old Pattern):**
```java
@SpringBootTest(classes = {
    MsBalanceApp.class,
    IntegrationTestConfiguration.class
})
public class SomeIntegrationTest {
    // ...
}
```

**After (New Pattern):**
```java
@SpringBootTest(classes = {
    TestLepConfiguration.class,  // <- PUT IT FIRST! ⭐
    MsBalanceApp.class,
    IntegrationTestConfiguration.class
})
public class SomeIntegrationTest {
    // ...
}
```

**🔴 CRITICAL:** TestLepConfiguration MUST be first in the classes array!

### Step 3: Update Test Methods Using LEP Context

**Before (Old Pattern):**
```java
@Autowired
private LepManager lepManager;

@Test
public void testSomething() {
    lepManager.beginThreadContext(ctx -> {
        ctx.setValue(THREAD_CONTEXT_KEY_TENANT_CONTEXT, tenantContextHolder.getContext());
        ctx.setValue(THREAD_CONTEXT_KEY_AUTH_CONTEXT, authContextHolder.getContext());
    });

    try {
        // test code
    } finally {
        lepManager.endThreadContext();
    }
}
```

**After (New Pattern):**
```java
@Autowired
private LepManagementService lepManagementService;

@Autowired
private TenantContextHolder tenantContextHolder;

@Test
public void testSomething() {
    // Set tenant BEFORE beginThreadContext
    TenantContextUtils.setTenant(tenantContextHolder, "TEST");

    try (var context = lepManagementService.beginThreadContext()) {
        // test code
    }
}
```

**Key Changes:**
1. Replace `LepManager` with `LepManagementService`
2. Use try-with-resources for automatic cleanup
3. Set tenant explicitly using `TenantContextUtils.setTenant()` BEFORE beginThreadContext
4. No need to manually set context values

### Step 4: Migrate @Before and @After Test Methods

**Before (Old Pattern with @Before/@After):**
```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MsBalanceApp.class})
public class LepIntegrationTest {

    @Autowired
    private LepManager lepManager;

    @Autowired
    private TenantContextHolder tenantContextHolder;

    @Before
    public void setup() {
        TenantContextUtils.setTenant(tenantContextHolder, "TEST_TENANT");
        lepManager.beginThreadContext(ctx -> {
            ctx.setValue(THREAD_CONTEXT_KEY_TENANT_CONTEXT, tenantContextHolder.getContext());
            ctx.setValue(THREAD_CONTEXT_KEY_AUTH_CONTEXT, authContextHolder.getContext());
        });
    }

    @After
    public void tearDown() {
        lepManager.endThreadContext();
        TenantContextUtils.clear();
    }

    @Test
    public void testLepExecution() {
        // test code that uses LEP
    }
}
```

**After (New Pattern with @BeforeEach/@AfterEach):**
```java
@SpringBootTest(classes = {
    TestLepConfiguration.class,  // ⭐ First!
    MsBalanceApp.class
})
public class LepIntegrationTest {

    @Autowired
    private LepManagementService lepManagementService;

    @Autowired
    private TenantContextHolder tenantContextHolder;

    @BeforeEach
    public void setup() {
        TenantContextUtils.setTenant(tenantContextHolder, "TEST_TENANT");
        lepManagementService.beginThreadContext();
    }

    @AfterEach
    public void tearDown() {
        lepManagementService.endThreadContext();
        TenantContextUtils.clear();
    }

    @Test
    public void testLepExecution() {
        // test code that uses LEP
    }
}
```

**Alternative: Using try-with-resources in each test (Recommended):**
```java
@SpringBootTest(classes = {
    TestLepConfiguration.class,  // ⭐ First!
    MsBalanceApp.class
})
public class LepIntegrationTest {

    @Autowired
    private LepManagementService lepManagementService;

    @Autowired
    private TenantContextHolder tenantContextHolder;

    @Test
    public void testLepExecution() {
        TenantContextUtils.setTenant(tenantContextHolder, "TEST_TENANT");

        try (var context = lepManagementService.beginThreadContext()) {
            // test code that uses LEP
        }
    }

    @Test
    public void testAnotherLepExecution() {
        TenantContextUtils.setTenant(tenantContextHolder, "ANOTHER_TENANT");

        try (var context = lepManagementService.beginThreadContext()) {
            // test code that uses LEP
        }
    }
}
```

**Key Migration Points for Test Methods:**

1. **@Before → @BeforeEach**
   - Change JUnit 4 `@Before` to JUnit 5 `@BeforeEach`
   - Replace `LepManager` with `LepManagementService`
   - Remove lambda-based context initialization
   - Keep `TenantContextUtils.setTenant()` BEFORE `beginThreadContext()`

2. **@After → @AfterEach**
   - Change JUnit 4 `@After` to JUnit 5 `@AfterEach`
   - Use `lepManagementService.endThreadContext()`
   - Keep `TenantContextUtils.clear()` for cleanup

3. **Try-with-resources Alternative (Preferred):**
   - Remove @BeforeEach/@AfterEach entirely
   - Set tenant in each test method
   - Use try-with-resources block for automatic cleanup
   - More explicit and safer - context is scoped to each test

**Migration Pattern Summary:**
```
Old Pattern                           New Pattern
────────────────────────────────────────────────────────────────────
@Before + LepManager                → @BeforeEach + LepManagementService
                                       OR
                                       Try-with-resources in each test

lepManager.beginThreadContext(λ)    → lepManagementService.beginThreadContext()

@After + lepManager.end...          → @AfterEach + lepManagement...end...
                                       OR
                                       Automatic with try-with-resources

Manual context.setValue(...)        → Automatic (framework handles it)

@RunWith(SpringRunner.class)        → (Not needed in JUnit 5)
```

### Step 5: Simple Test Migration (Minimal Changes)

**If running FULL Spring context in tests**, add this bean to test context:

```java
@Override
public LepUpdateMode lepUpdateMode() {
    return LepUpdateMode.SYNCHRONOUS;
}
```

This allows most tests to run without major refactoring.

---

## 11. CoreContextsHolder Removal from LEPs

**Action:** Update LEP Groovy scripts to use LepThreadHelper

**Before (Old Pattern in LEP scripts):**
```groovy
// In LEP script
CoreContextsHolder.get().runInThread(executor) {
    // your task
}
```

**After (New Pattern in LEP scripts):**
```groovy
// In LEP script
lepContext.thread.runInThread(executor) {
    // your task
}
```

**Key Changes:**
1. Replace `CoreContextsHolder.get()` with `lepContext.thread`
2. `lepContext.thread` is a `LepThreadHelper` instance provided by BaseLepContext
3. CoreContextsHolder is deprecated and will be removed in future versions

**Note:** This change is for LEP Groovy scripts themselves, not Java code.

---

## 12. Verification Steps

After completing the migration, verify everything works correctly:

### 12.1 Build Verification
```bash
./gradlew clean build
```
**Expected:** Build should complete successfully without errors.

### 12.2 Test Verification
```bash
./gradlew test
```
**Expected:** All tests should pass.

### 12.3 Application Startup Verification
```bash
./gradlew bootRun
```
**Expected:** Application should start without errors. Look for LEP-related log messages.

### 12.4 LEP Execution Verification
1. Deploy a simple LEP script to test tenant
2. Execute an operation that triggers the LEP
3. Verify LEP executes correctly and can access lepContext fields

**Example test LEP script:**
```groovy
// Test LEP to verify context access
def result = [
    tenant: lepContext.authContext.tenant.orElse("UNKNOWN"),
    services: lepContext.services != null,
    templates: lepContext.templates != null,
    traceService: lepContext.traceService != null
]
return result
```

### 12.5 Key Things to Check

**In Logs:**
- [ ] No LEP-related errors on startup
- [ ] LEP scripts compile successfully
- [ ] Context initialization messages appear

**In Application:**
- [ ] All LEP extension points still work
- [ ] LEP context fields are accessible
- [ ] Key resolvers generate correct keys
- [ ] Multi-threading works correctly

**In Tests:**
- [ ] All integration tests pass
- [ ] LEP-specific tests pass
- [ ] No context initialization errors

---

## Common Migration Issues and Solutions

### Issue 1: "Cannot find LepContext class"
**Solution:** Make sure your LepContext class is named exactly `LepContext` (naming convention).

### Issue 2: Tests fail with "No LepUpdateMode bean"
**Solution:** Add `lepUpdateMode()` method returning `SYNCHRONOUS` to your test configuration.

### Issue 3: "Tenant context not found"
**Solution:** Make sure to call `TenantContextUtils.setTenant()` BEFORE `beginThreadContext()` in tests.

### Issue 4: LEP scripts can't access services
**Solution:**
1. Check that LepContextFactory properly initializes all fields in buildLepContext()
2. Verify the structure matches what was in the old listener's processBeforeRefresh
3. Ensure all fields are public in LepServices/LepTemplates nested classes

### Issue 5: Key resolvers not found
**Solution:** Make sure resolver classes are in component scan path and annotated with @Component.

### Issue 6: Build fails with dependency resolution errors
**Solution:** Clean gradle cache and rebuild:
```bash
./gradlew clean
rm -rf ~/.gradle/caches/
./gradlew build
```

### Issue 7: "Method not found" in LEP scripts
**Solution:**
1. Verify that all services are properly exposed through LepContext.services
2. Check that fields are marked as public (not private)
3. Verify field names match what LEP scripts expect

### Issue 8: Thread context leaks in tests
**Solution:** Always use try-with-resources or proper try-finally blocks to ensure context cleanup.

### Issue 9: "Context structure mismatch" - LEP can't find expected fields
**Solution:**
1. Compare your new LepContext with the old listener's processBeforeRefresh
2. Ensure exact field name matches (including any typos that existed before)
3. Verify nested class structure is identical

### Issue 10: Tests fail with "LepContextFactory bean not found"
**Solution:** Ensure TestLepConfiguration is listed FIRST in @SpringBootTest classes array.

---

## Migration Checklist

Use this checklist to track your progress:

**Dependencies & Configuration:**
- [ ] 1. Updated build.gradle with new LEP dependencies
- [ ] 2. Updated gradle.properties with new xm_commons_version (2.3.37+)
- [ ] 3. Migrated LepConfiguration to extend GroovyLepEngineConfiguration
- [ ] 4. Removed @EnableLepServices annotation

**LEP Context Migration:**
- [ ] 5. Found old listener class that extends SpringLepProcessingApplicationListener
- [ ] 6. Documented the context structure from processBeforeRefresh method
- [ ] 7. Created/verified LepContext.java with preserved structure
- [ ] 8. Refactored LepContext to extend BaseLepContext
- [ ] 9. Added marker interfaces to LepContext (TraceServiceField, etc.)
- [ ] 10. Changed nested class fields to public
- [ ] 11. Created new LepContextFactory with same initialization logic
- [ ] 12. Verified buildLepContext matches old processBeforeRefresh
- [ ] 13. Deleted old *LepProcessingApplicationListener class

**Code Migration:**
- [ ] 14. Migrated all LEP key resolvers to implement LepKeyResolver
- [ ] 15. Updated segments() methods in resolvers
- [ ] 16. Converted @LepService interfaces to classes
- [ ] 17. Updated thread context management to use lepManagementService
- [ ] 18. Updated WebMvcConfiguration (if applicable)
- [ ] 19. Added application properties for LEP config (optional)

**Test Migration:**
- [ ] 20. Created TestLepConfiguration extending GroovyLepEngineConfiguration
- [ ] 21. Added lepUpdateMode() = SYNCHRONOUS to test config
- [ ] 22. Updated test class annotations (TestLepConfiguration first)
- [ ] 23. Updated test methods to use LepManagementService
- [ ] 24. Migrated @Before/@After to @BeforeEach/@AfterEach or try-with-resources
- [ ] 25. Removed manual context value setting in tests

**LEP Scripts & Cleanup:**
- [ ] 26. Removed CoreContextsHolder usage from LEP scripts
- [ ] 27. Updated LEP scripts to use lepContext.thread

**Verification:**
- [ ] 28. Verified build passes
- [ ] 29. Verified all tests pass
- [ ] 30. Verified application starts successfully
- [ ] 31. Verified LEP scripts execute correctly
- [ ] 32. Tested with actual tenant data
- [ ] 33. Verified all services accessible in LEP context

---

## Reference Materials

### Official Documentation
- Migration Guide: `/xm-commons/xm-commons-lep/MIGRATION_GUIDE.MD`

### Reference Pull Requests
1. xm-uaa: https://github.com/xm-online/xm-uaa/pull/266/files
2. tmf-ms-activation: https://github.com/xm-online/tmf-ms-activation/pull/142/files
3. xm-ms-entity: https://github.com/xm-online/xm-ms-entity/pull/369/files
4. xm-ms-template: https://github.com/xm-online/xm-ms-template/pull/8/files

### Key Classes to Understand
- `BaseLepContext` - Base class providing standard LEP context fields
- `LepContextFactory` - Interface for building LEP contexts
- `LepKeyResolver` - Interface for custom key resolution
- `GroovyLepEngineConfiguration` - Base configuration for Groovy LEP engine
- `LepManagementService` - Service for managing LEP thread contexts
- `LepThreadHelper` - Helper for running tasks in new threads with context

---

## Advanced Topics (For Libraries and Commons)

### Adding Global LEP Context Fields (Commons/Libraries Only)

If you're developing xm-commons or a library that needs to add fields globally to all LEP contexts:

**Implement LepAdditionalContext:**
```java
@Component
public class TraceService implements LepAdditionalContext {

    private static final String FIELD_NAME = "traceService";

    @Override
    public String additionalContextKey() {
        return FIELD_NAME;
    }

    @Override
    public TraceService additionalContextValue() {
        return this;
    }

    @Override
    public Class<? extends LepAdditionalContextField> fieldAccessorInterface() {
        return TraceServiceField.class;
    }
}
```

**Create Field Accessor Interface:**
```java
public interface TraceServiceField extends LepAdditionalContextField {
    String FIELD_NAME = "traceService";

    default TraceService getTraceService() {
        return (TraceService) get(FIELD_NAME);
    }
}
```

**Usage in Microservices:**
Microservices implement the interface in their LepContext:
```java
public class LepContext extends BaseLepContext implements TraceServiceField {
    // Field is automatically available via interface
}
```

**Access in LEP Scripts:**
```groovy
lepContext.traceService.someMethod()
```

### Library Dependencies

For xm-commons or libraries, use minimal dependencies:

**In library build.gradle:**
```groovy
implementation project(':xm-commons-lep')  // Only base LEP
testImplementation project(':xm-commons-lep-groovy')  // Groovy engine for tests
```

This allows target microservices to choose their own LEP engine (Groovy or JS).

---

## Summary of Key Architecture Changes

### Old LEP Engine
- Annotation-based service enablement (@EnableLepServices)
- Manual context value setting with lambdas
- Separator-based key resolution with complex GroupMode
- Interface-based @LepService
- Event listener for context initialization (SpringLepProcessingApplicationListener)
- WebLepSpringConfiguration base class

### New LEP Engine
- Factory-based context creation (LepContextFactory)
- Automatic context management with try-with-resources
- Simplified list-based key segments
- Class-based @LepService
- Base context inheritance model (BaseLepContext)
- GroovyLepEngineConfiguration base class
- Type-safe global fields via marker interfaces

### Benefits of New Engine
1. **Simpler API** - Less boilerplate code
2. **Type Safety** - Compile-time checking for context fields
3. **Better Resource Management** - Automatic cleanup with try-with-resources
4. **Easier Testing** - Synchronous mode for deterministic tests
5. **Extensibility** - Global fields via marker interfaces
6. **Performance** - Script warmup and better compilation
7. **Maintainability** - Clearer separation of concerns
8. **Consistency** - Uniform context structure across microservices

---

## Key Takeaways

### Most Important Points to Remember:

1. **⭐ PRESERVE LEP CONTEXT STRUCTURE** - The structure in LepContext and LepContextFactory must EXACTLY match what was in the old SpringLepProcessingApplicationListener's processBeforeRefresh method

2. **TestLepConfiguration MUST be first** in @SpringBootTest classes array

3. **Set tenant BEFORE beginThreadContext** in tests

4. **Use LepManagementService** instead of LepManager

5. **All nested class fields must be public** in LepServices/LepTemplates

6. **LepContext class name is a convention** - must be named exactly "LepContext"

7. **Clean gradle cache** if you encounter dependency resolution issues

8. **Try-with-resources is preferred** for thread context management

---

## End of Migration Guide

This guide is AI-friendly and designed to be comprehensive. Follow the steps in order, paying special attention to preserving the LEP context structure from the old listener. Refer to the reference PRs for real-world examples. If you encounter issues not covered here, check the common issues section or consult the xm-commons documentation.

**Last Updated:** Based on xm-commons version 2.3.37+ (but version have to start from 2.3) and migrations completed in 2023-2024.
**Guide Version:** 2.0 - Enhanced with context preservation and test migration details.
