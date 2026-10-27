package com.icthh.xm.ms.balance.config;

import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Balance.
 * <p>
 * Properties are configured in the application.yml file.
 * </p>
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Getter
@Setter
public class ApplicationProperties {

    private final Retry retry = new Retry();

    private List<String> tenantIgnoredPathList = Collections.emptyList();
    private List<String> tenantWithCreationAccessList = Collections.emptyList();
    private boolean timelinesEnabled;
    private boolean kafkaEnabled;
    private String kafkaSystemQueue;
    private Integer pocketChargingBatchSize;
    private String specificationPathPattern;
    private String specificationName;
    private String dbSchemaSuffix;
    private String configPrivateKey;
    private final ApiDocs apiDocs = new ApiDocs();
    private final Prometheus prometheus = new Prometheus();
    private final Management management = new Management();
    private final TenantCache tenantCache = new TenantCache();
    private final TenantMemoryCache tenantMemoryCache = new TenantMemoryCache();
    private final XmAutoConfigurationFilter xmAutoConfigurationFilter = new XmAutoConfigurationFilter();

    @Getter
    @Setter
    private static class Retry {

        private int maxAttempts;
        private long delay;
        private int multiplier;
    }

    private LepProperties lep;

    @Data
    public static class LepProperties {
        private String tenantScriptStorage;
        private Boolean fullRecompileOnLepUpdate;
    }

    @Data
    public static class ApiDocs {
        private boolean privilegeCheck;
    }

    public static class Prometheus {
        private boolean publicAccess;

        public boolean isPublic() {
            return publicAccess;
        }

        public void setPublic(boolean publicAccess) {
            this.publicAccess = publicAccess;
        }
    }

    @Data
    public static class TenantCache {
        private String enabled;
    }

    @Data
    public static class TenantMemoryCache {
        private String enabled;
    }

    @Data
    public static class XmAutoConfigurationFilter {
        private boolean disabled;
    }

    @Data
    public static class Management {
        private final Access access = new Access();
        private final Info info = new Info();
        private final Logs logs = new Logs();

        @Data
        public static class Access {
            private String role;
        }

        public static class Info {
            private boolean publicAccess;

            public boolean isPublic() {
                return publicAccess;
            }

            public void setPublic(boolean publicAccess) {
                this.publicAccess = publicAccess;
            }
        }

        @Data
        public static class Logs {
            private boolean systemAdminOnly;
        }
    }

}
