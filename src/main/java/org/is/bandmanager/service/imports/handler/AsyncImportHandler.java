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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processImport(Long operationId, byte[] fileContent, String originalFilename, String mimeType, String username) {
		ImportOperation operation = repository.findById(operationId).orElseThrow(() -> new ServiceException(BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND, "ImportOperation", operationId));
		try {
			log.debug("Starting import processing for operation {}: {}", operation.getId(), originalFilename);
			operation.setStatus(ImportStatus.PROCESSING);
			operation.setErrorMessage(null);
			operation.setCreatedEntitiesCount(null);

			var stored = storeImportFile(operation, fileContent, originalFilename, mimeType);
			if (stored != null) {
				operation.setStorageObjectKey(stored.objectKey());
				operation.setContentType(stored.contentType());
				operation.setFileSize(stored.size());
			}

			List<MusicBandImportRequest> importRequests = fileParserFacade.parseFile(fileContent, originalFilename, mimeType);
			log.debug("Parsed {} records from file {}", importRequests.size(), originalFilename);
			if (importRequests.isEmpty()) {
				throw new IllegalArgumentException("Import file is empty");
			}

			List<Long> createdBandIds = processor.processImport(importRequests, username);
			operation.setStatus(ImportStatus.COMPLETED);
			operation.setCreatedEntitiesCount(createdBandIds.size());
			log.info("Import operation {} completed successfully. Created {} MusicBand entities", operation.getId(), createdBandIds.size());
		} catch (ValidationException e) {
			operation.setStatus(ImportStatus.VALIDATION_FAILED);
			operation.setErrorMessage(getErrorMessage(e));
			operation.setCreatedEntitiesCount(null);
		} catch (Exception e) {
			operation.setStatus(ImportStatus.FAILED);
			operation.setErrorMessage(getErrorMessage(e));
			operation.setCreatedEntitiesCount(null);
		} finally {
			operation.setCompletedAt(LocalDateTime.now());
			repository.save(operation);
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
			return null;
		}
	}

}
