package org.is.bandmanager.service.imports.model;

public enum ImportStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
	FINALIZING_FILE,
	VALIDATION_FAILED
}