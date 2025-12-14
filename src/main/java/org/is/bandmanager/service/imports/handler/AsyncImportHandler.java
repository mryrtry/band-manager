package org.is.bandmanager.service.imports.handler;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.bandmanager.dto.importRequest.MusicBandImportRequest;
import org.is.bandmanager.exception.message.BandManagerErrorMessage;
import org.is.bandmanager.service.imports.model.ImportOperation;
import org.is.bandmanager.service.imports.model.ImportStatus;
import org.is.bandmanager.service.imports.parser.FileParserFacade;
import org.is.bandmanager.service.imports.processor.MusicBandImportProcessor;
import org.is.bandmanager.service.imports.repository.ImportOperationRepository;
import org.is.bandmanager.service.storage.ImportStorageService;
import org.is.bandmanager.service.storage.StoredObjectMetadata;
import org.is.exception.ServiceException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncImportHandler implements ImportHandler {

	private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

	private final FileParserFacade fileParserFacade;

	private final MusicBandImportProcessor processor;

	private final ImportOperationRepository repository;

	private final ImportStorageService storageService;

	@Override
	@Async("importTaskExecutor")
	public void processImport(Long operationId, byte[] fileContent, String originalFilename, String mimeType, String username) {
		ImportOperation operation = repository.findById(operationId).orElseThrow(() -> new ServiceException(BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND, "ImportOperation", operationId));
		boolean fileStored = false;
		try {
			log.debug("Starting import processing for operation {}: {}", operation.getId(), originalFilename);
			operation.setStatus(ImportStatus.PROCESSING);
			operation = repository.save(operation);

			var stored = storeImportFile(operation, fileContent, originalFilename, mimeType);
			fileStored = stored != null;
			if (stored != null) {
				operation.setStorageObjectKey(stored.objectKey());
				operation.setContentType(stored.contentType());
				operation.setFileSize(stored.size());
				operation = repository.save(operation);
			}

			List<MusicBandImportRequest> importRequests = fileParserFacade.parseFile(fileContent, originalFilename, mimeType);
			log.debug("Parsed {} records from file {}", importRequests.size(), originalFilename);
			if (importRequests.isEmpty()) {
				throw new IllegalArgumentException("Import file is empty");
			}

			List<Long> createdBandIds = processor.processImport(importRequests, username);
			operation.setStatus(ImportStatus.COMPLETED);
			operation.setCreatedEntitiesCount(createdBandIds.size());
			operation.setCompletedAt(LocalDateTime.now());
			log.info("Import operation {} completed successfully. Created {} MusicBand entities", operation.getId(), createdBandIds.size());
		} catch (ValidationException e) {
			operation.setStatus(ImportStatus.VALIDATION_FAILED);
			operation.setErrorMessage(getErrorMessage(e));
		} catch (Exception e) {
			operation.setStatus(ImportStatus.FAILED);
			operation.setErrorMessage(getErrorMessage(e));
		} finally {
			operation.setCompletedAt(LocalDateTime.now());
			saveFresh(operationId, operation, fileStored);
		}
	}

	private String getErrorMessage(Exception e) {
		String message = e.getMessage();
		if (message != null && message.length() > MAX_ERROR_MESSAGE_LENGTH) {
			message = message.substring(0, MAX_ERROR_MESSAGE_LENGTH) + "... [truncated]";
		}
		return message;
	}

	private StoredObjectMetadata storeImportFile(ImportOperation operation, byte[] fileContent, String originalFilename, String mimeType) {
		try {
			return storageService.storeImportFile(operation.getId(), originalFilename, mimeType, fileContent);
		} catch (Exception e) {
			log.warn("Failed to store import file id={} filename={}: {}", operation.getId(), originalFilename, e.getMessage());
			operation.setErrorMessage("Import file not stored: " + e.getMessage());
			throw e;
		}
	}

	private void saveFresh(Long operationId, ImportOperation updates, boolean fileStored) {
		try {
			ImportOperation fresh = repository.findById(operationId)
					.orElseThrow(() -> new ServiceException(BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND, "ImportOperation", operationId));
			fresh.setStatus(updates.getStatus());
			fresh.setCreatedEntitiesCount(updates.getCreatedEntitiesCount());
			fresh.setErrorMessage(updates.getErrorMessage());
			fresh.setCompletedAt(updates.getCompletedAt());
			if (updates.getStorageObjectKey() != null) {
				fresh.setStorageObjectKey(updates.getStorageObjectKey());
				fresh.setContentType(updates.getContentType());
				fresh.setFileSize(updates.getFileSize());
			}
			repository.save(fresh);
		} catch (Exception persistenceException) {
			if (fileStored && updates.getStorageObjectKey() != null) {
				storageService.deleteQuietly(updates.getStorageObjectKey());
			}
			throw persistenceException;
		}
	}

}
