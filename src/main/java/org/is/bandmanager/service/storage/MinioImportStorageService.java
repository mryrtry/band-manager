package org.is.bandmanager.service.storage;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.bandmanager.config.StorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioImportStorageService implements ImportStorageService {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final MinioClient minioClient;
    private final StorageProperties storageProperties;

    @PostConstruct
    public void ensureBucketPresent() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(storageProperties.getBucket())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(storageProperties.getBucket())
                        .build());
                log.info("Created missing bucket {}", storageProperties.getBucket());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize bucket " + storageProperties.getBucket(), e);
        }
    }

    @Override
	public StoredObjectMetadata storeImportFile(Long operationId, String originalFilename, String contentType, byte[] content) {
		String objectKey = buildObjectKey(operationId, originalFilename);
		String resolvedContentType = StringUtils.hasText(contentType) ? contentType : DEFAULT_CONTENT_TYPE;
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
			var builder = PutObjectArgs.builder()
					.bucket(storageProperties.getBucket())
					.object(objectKey)
					.stream(inputStream, content.length, -1)
					.contentType(resolvedContentType);
			if (StringUtils.hasText(originalFilename)) {
				builder.headers(Map.of("X-Amz-Meta-original-filename", originalFilename));
			}
			minioClient.putObject(builder.build());
			log.debug("Stored import file {} in bucket {} as {}", originalFilename, storageProperties.getBucket(), objectKey);
			return new StoredObjectMetadata(objectKey, resolvedContentType, content.length);
		} catch (Exception e) {
			throw new StorageException("Failed to store import file: " + originalFilename, e);
		}
    }

	@Override
	public StoredObjectResource loadImportFile(String objectKey, String originalFilename, String contentType, Long sizeHint) {
		try {
			var stat = minioClient.statObject(StatObjectArgs.builder()
					.bucket(storageProperties.getBucket())
					.object(objectKey)
					.build());
			var response = minioClient.getObject(GetObjectArgs.builder()
					.bucket(storageProperties.getBucket())
					.object(objectKey)
					.build());
			String resolvedContentType = StringUtils.hasText(contentType) ? contentType : stat.contentType();
			long size = sizeHint != null ? sizeHint : stat.size();
			return new StoredObjectResource(originalFilename, resolvedContentType, size, response);
		} catch (Exception e) {
			throw new StorageException("Failed to download import file: " + originalFilename, e);
		}
	}

    @Override
    public void deleteQuietly(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(storageProperties.getBucket())
                    .object(objectKey)
                    .build());
            log.debug("Removed object {} from bucket {}", objectKey, storageProperties.getBucket());
        } catch (Exception e) {
            log.warn("Failed to delete object {} from bucket {}: {}", objectKey, storageProperties.getBucket(), e.getMessage());
        }
	}

	private String buildObjectKey(Long operationId, String filename) {
		String safeName = StringUtils.hasText(filename) ? filename : "import-file";
		String sanitized = safeName.replaceAll("\\s+", "_");
		return "imports/op-" + operationId + "/" + System.currentTimeMillis() + "-" + sanitized;
	}

}
