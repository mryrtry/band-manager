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
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;


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
		ImportOperation operation = repository.findById(operationId)
				.orElseThrow(() -> new ServiceException(BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND, "ImportOperation", operationId));
		try {
			log.debug("Starting import processing for operation {}: {}", operation.getId(), originalFilename);
			updateOperation(operationId, op -> {
				op.setStatus(ImportStatus.PROCESSING);
				op.setErrorMessage(null);
				op.setCreatedEntitiesCount(null);
			});

			var stored = storeImportFile(operationId, fileContent, originalFilename, mimeType);
			if (stored != null) {
				updateOperation(operationId, op -> {
					op.setStorageObjectKey(stored.objectKey());
					op.setContentType(stored.contentType());
					op.setFileSize(stored.size());
				});
			}

			List<MusicBandImportRequest> importRequests = fileParserFacade.parseFile(fileContent, originalFilename, mimeType);
			log.debug("Parsed {} records from file {}", importRequests.size(), originalFilename);
			if (importRequests.isEmpty()) {
				throw new IllegalArgumentException("Import file is empty");
			}

			List<Long> createdBandIds = processor.processImport(importRequests, username);
			updateOperation(operationId, op -> {
				op.setStatus(ImportStatus.COMPLETED);
				op.setCreatedEntitiesCount(createdBandIds.size());
			});
			log.info("Import operation {} completed successfully. Created {} MusicBand entities", operation.getId(), createdBandIds.size());
		} catch (ValidationException e) {
			updateOperation(operationId, op -> {
				op.setStatus(ImportStatus.VALIDATION_FAILED);
				op.setErrorMessage(getErrorMessage(e));
				op.setCreatedEntitiesCount(null);
			});
		} catch (Exception e) {
			updateOperation(operationId, op -> {
				op.setStatus(ImportStatus.FAILED);
				op.setErrorMessage(getErrorMessage(e));
				op.setCreatedEntitiesCount(null);
			});
		} finally {
			updateOperation(operationId, op -> op.setCompletedAt(LocalDateTime.now()));
		}
	}

	private String getErrorMessage(Exception e) {
		String message = e.getMessage();
		if (message != null && message.length() > MAX_ERROR_MESSAGE_LENGTH) {
			message = message.substring(0, MAX_ERROR_MESSAGE_LENGTH) + "... [truncated]";
		}
		return message;
	}

	private StoredObjectMetadata storeImportFile(Long operationId, byte[] fileContent, String originalFilename, String mimeType) {
		try {
			return storageService.storeImportFile(operationId, originalFilename, mimeType, fileContent);
		} catch (Exception e) {
			log.warn("Failed to store import file id={} filename={}: {}", operationId, originalFilename, e.getMessage());
			updateOperation(operationId, op -> op.setErrorMessage("Import file not stored: " + e.getMessage()));
			return null;
		}
	}

	private void updateOperation(Long operationId, Consumer<ImportOperation> consumer) {
		int attempts = 0;
		while (attempts < 2) {
			try {
				ImportOperation op = repository.findById(operationId)
						.orElseThrow(() -> new ServiceException(BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND, "ImportOperation", operationId));
				consumer.accept(op);
				repository.saveAndFlush(op);
				return;
			} catch (OptimisticLockingFailureException ex) {
				attempts++;
				if (attempts >= 2) {
					throw ex;
				}
				log.debug("Optimistic lock retry for import operation {} (attempt {})", operationId, attempts + 1);
			}
		}
	}

}
