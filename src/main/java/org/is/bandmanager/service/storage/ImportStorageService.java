package org.is.bandmanager.service.storage;

public interface ImportStorageService {

	String putStaging(Long operationId, String originalFilename, String contentType, byte[] content);

	String finalizeFromStaging(String stagingKey, Long operationId, String originalFilename);

	StoredObjectResource loadImportFile(String objectKey, String originalFilename, String contentType, Long sizeHint);

	void deleteObject(String objectKey);
}
