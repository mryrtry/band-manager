package org.is.bandmanager.service.storage;

import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
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
import java.util.UUID;


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
			boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(storageProperties.getBucket()).build());
			if (!exists) {
				minioClient.makeBucket(MakeBucketArgs.builder().bucket(storageProperties.getBucket()).build());
			}
		} catch (Exception e) {
			throw new IllegalStateException("Failed to init bucket", e);
		}
	}

	@Override
	public String putStaging(Long operationId, String originalFilename, String contentType, byte[] content) {

		String key = buildStagingKey(operationId, originalFilename);
		String resolvedType = StringUtils.hasText(contentType) ? contentType : DEFAULT_CONTENT_TYPE;

		try (ByteArrayInputStream in = new ByteArrayInputStream(content)) {
			PutObjectArgs.Builder builder = PutObjectArgs.builder().bucket(storageProperties.getBucket()).object(key).stream(in, content.length, -1).contentType(resolvedType);

			if (StringUtils.hasText(originalFilename)) {
				builder.headers(Map.of("X-Amz-Meta-original-filename", originalFilename));
			}

			minioClient.putObject(builder.build());
			return key;
		} catch (Exception e) {
			throw new StorageException("Failed to store staging object", e);
		}
	}

	@Override
	public String finalizeFromStaging(String stagingKey, Long operationId, String originalFilename) {

		String finalKey = buildFinalKey(operationId, originalFilename);

		try {
			minioClient.copyObject(CopyObjectArgs.builder().bucket(storageProperties.getBucket()).object(finalKey).source(CopySource.builder().bucket(storageProperties.getBucket()).object(stagingKey).build()).build());

			minioClient.removeObject(RemoveObjectArgs.builder().bucket(storageProperties.getBucket()).object(stagingKey).build());

			return finalKey;
		} catch (Exception e) {
			throw new StorageException("Failed to finalize import file", e);
		}
	}

	@Override
	public StoredObjectResource loadImportFile(String objectKey, String originalFilename, String contentType, Long sizeHint) {
		try {
			var stat = minioClient.statObject(StatObjectArgs.builder().bucket(storageProperties.getBucket()).object(objectKey).build());

			var stream = minioClient.getObject(GetObjectArgs.builder().bucket(storageProperties.getBucket()).object(objectKey).build());

			String resolvedType = StringUtils.hasText(contentType) ? contentType : stat.contentType();
			long size = sizeHint != null ? sizeHint : stat.size();

			return new StoredObjectResource(originalFilename, resolvedType, size, stream);
		} catch (Exception e) {
			throw new StorageException("Failed to load import file", e);
		}
	}

	@Override
	public void deleteObject(String objectKey) {
		if (!StringUtils.hasText(objectKey))
			return;
		try {
			minioClient.removeObject(RemoveObjectArgs.builder().bucket(storageProperties.getBucket()).object(objectKey).build());
		} catch (Exception ignored) {
		}
	}

	private String buildStagingKey(Long operationId, String filename) {
		return "staging/op-" + operationId + "/" + UUID.randomUUID() + "-" + sanitize(filename);
	}

	private String buildFinalKey(Long operationId, String filename) {
		return "imports/op-" + operationId + "/" + sanitize(filename);
	}

	private String sanitize(String filename) {
		if (!StringUtils.hasText(filename)) {
			return "import-file";
		}
		return filename.replaceAll("\\s+", "_");
	}

}
