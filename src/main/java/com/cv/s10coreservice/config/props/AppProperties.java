package com.cv.s10coreservice.config.props;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Info info;
    private OrgService orgService;
    private UnitService unitService;
    private NotifyService notifyService;

    @Data
    public static class Info {
        private String author;
        private String email;
        private String description;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class OrgService extends GenericProps {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class UnitService extends GenericProps {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class NotifyService extends GenericProps {
    }

    @Data
    public static class GenericProps {
        private String name;
        private String contextPath;
    }
}
