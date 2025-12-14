package org.is.bandmanager.config;

import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import io.minio.MinioClient;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class MinioConfig {

    @Bean
    public MinioClient minioClient(StorageProperties properties) {
        Assert.hasText(properties.getEndpoint(), "storage.s3.endpoint must be configured");
        Assert.hasText(properties.getAccessKey(), "storage.s3.access-key must be configured");
        Assert.hasText(properties.getSecretKey(), "storage.s3.secret-key must be configured");
        Assert.hasText(properties.getBucket(), "storage.s3.bucket must be configured");

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout())
                .readTimeout(properties.getReadTimeout())
                .writeTimeout(properties.getWriteTimeout())
                .build();

        String endpoint = properties.getEndpoint();
        if (endpoint != null && !endpoint.startsWith("http")) {
            endpoint = (properties.isSecure() ? "https://" : "http://") + endpoint;
        }

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .httpClient(httpClient)
                .build();
    }
}
