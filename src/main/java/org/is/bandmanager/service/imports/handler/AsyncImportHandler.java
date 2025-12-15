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
import org.is.bandmanager.service.storage.StorageException;
import org.is.exception.ServiceException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncImportHandler implements ImportHandler {

	private final FileParserFacade fileParserFacade;
	private final MusicBandImportProcessor processor;
	private final ImportOperationRepository repository;
	private final ImportStorageService storageService;

	@Override
	@Async("importTaskExecutor")
	public void processImport(Long operationId,
	                          byte[] fileContent,
	                          String originalFilename,
	                          String mimeType,
	                          String username) {

		ImportOperation operation = repository.findById(operationId)
				.orElseThrow(() -> new ServiceException(
						BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND,
						"ImportOperation",
						operationId
				));

		String stagingKey = null;

		try {
			operation.setStatus(ImportStatus.PROCESSING);
			operation = repository.save(operation);

			// Pre commit: minio
			stagingKey = storageService.putStaging(
					operationId,
					originalFilename,
					mimeType,
					fileContent
			);

			operation.setStagingObjectKey(stagingKey);
			operation = repository.save(operation);

			// Pre commit: bd
			List<MusicBandImportRequest> requests =
					fileParserFacade.parseFile(fileContent, originalFilename, mimeType);

			if (requests.isEmpty()) {
				throw new IllegalArgumentException("Import file is empty");
			}

			// Commit: bd
			List<Long> createdIds = processor.processImport(requests, username);

			// Commit: minio
			String finalKey = storageService.finalizeFromStaging(
					stagingKey,
					operationId,
					originalFilename
			);

			// Set success
			operation.setStorageObjectKey(finalKey);
			operation.setCreatedEntitiesCount(createdIds.size());
			operation.setStatus(ImportStatus.COMPLETED);
		} catch (ValidationException e) {
			operation.setStatus(ImportStatus.VALIDATION_FAILED);
			operation.setErrorMessage(e.getMessage());
			operation.setCreatedEntitiesCount(null);
			cleanup(stagingKey);
		} catch (StorageException e) {
			operation.setStatus(ImportStatus.FAILED);
			operation.setErrorMessage("Failed to store import file %s".formatted(originalFilename));
			operation.setCreatedEntitiesCount(null);
			cleanup(stagingKey);
		} catch (Exception e) {
			operation.setStatus(ImportStatus.FAILED);
			operation.setErrorMessage("Import failed for file %s".formatted(originalFilename));
			operation.setCreatedEntitiesCount(null);
			cleanup(stagingKey);
		} finally {
			operation.setCompletedAt(LocalDateTime.now());
			repository.save(operation);
		}
	}

	private void cleanup(String stagingKey) {
		if (stagingKey == null) return;
		try {
			storageService.deleteObject(stagingKey);
		} catch (Exception ignored) {
		}
	}
}
