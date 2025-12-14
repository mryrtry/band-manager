package org.is.bandmanager.service.storage;

public interface ImportStorageService {

    StoredObjectMetadata storeImportFile(Long operationId, String originalFilename, String contentType, byte[] content);

    StoredObjectResource loadImportFile(String objectKey, String originalFilename, String contentType, Long sizeHint);

    void deleteQuietly(String objectKey);

}
