package org.is.bandmanager.service.storage;

public record StoredObjectMetadata(String objectKey, String contentType, long size) {
}
