package com.dazzle.asklepios.attachments;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "setup.attachments")
public class AttachmentProperties {
    private String bucket;
    private String region;
    private String endpoint;
    private String cdnEndpoint;
    private int presignExpirySeconds;
    private long maxBytes;
    private java.util.Set<String> allowed;
    private String accessKeyId;
    private String secretAccessKey;
}
