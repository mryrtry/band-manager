package org.is.bandmanager.service.imports.orchestrator;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportOrchestrator {

	private final ImportOperationRepository repository;
	private final FileParserFacade fileParserFacade;
	private final MusicBandImportProcessor processor;
	private final ImportStorageService storageService;
	private final PlatformTransactionManager txManager;

	public void run(Long operationId,
	                byte[] content,
	                String originalFilename,
	                String mimeType,
	                String username) {

		String stagingKey = null;

		try {
			log.info("Import started opId={} file={} mimeType={} user={}",
					operationId, originalFilename, mimeType, username);

			// minio prepare
			stagingKey = storageService.putStaging(operationId, originalFilename, mimeType, content);
			log.info("Import opId={} stagingKey={}", operationId, stagingKey);

			// parse
			List<MusicBandImportRequest> requests = fileParserFacade.parseFile(content, originalFilename, mimeType);
			if (requests.isEmpty()) {
				throw new IllegalArgumentException("Import file is empty");
			}
			log.info("Import opId={} parsedRecords={}", operationId, requests.size());

			final String finalStagingKey = stagingKey;
			final List<MusicBandImportRequest> finalRequests = requests;
			final String finalUsername = username;
			final String finalOriginalFilename = originalFilename;

			// db commit
			TransactionTemplate tt = new TransactionTemplate(txManager);
			DbCommitResult dbResult = tt.execute(txStatus -> {
				ImportOperation op = repository.findById(operationId).orElseThrow(() ->
						new ServiceException(
								BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND,
								"ImportOperation",
								operationId
						)
				);

				op.setStatus(ImportStatus.PROCESSING);
				op.setStagingObjectKey(finalStagingKey);
				op = repository.save(op);

				List<Long> createdIds = processor.processImport(finalRequests, finalUsername);

				op.setCreatedEntitiesCount(createdIds.size());
				op.setStatus(ImportStatus.FINALIZING_FILE);
				op = repository.save(op);

				return new DbCommitResult(createdIds.size());
			});

			if (dbResult == null) {
				throw new IllegalStateException("DB transaction returned null result");
			}

			log.info("Import opId={} dbCommitted created={}", operationId, dbResult.createdCount);

			// minio commit
			final String finalKey = storageService.finalizeFromStaging(finalStagingKey, operationId, finalOriginalFilename);
			log.info("Import opId={} fileFinalized finalKey={}", operationId, finalKey);

			TransactionTemplate tt2 = new TransactionTemplate(txManager);
			tt2.executeWithoutResult(txStatus -> {
				ImportOperation op2 = repository.findById(operationId).orElseThrow(() ->
						new ServiceException(
								BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND,
								"ImportOperation",
								operationId
						)
				);

				op2.setStorageObjectKey(finalKey);
				op2.setStatus(ImportStatus.COMPLETED);
				op2.setCompletedAt(LocalDateTime.now());
				op2 = repository.save(op2);
			});

			log.info("Import opId={} completed", operationId);

		} catch (ValidationException e) {
			log.warn("Import opId={} validationFailed: {}", operationId, e.getMessage(), e);
			fail(operationId, ImportStatus.VALIDATION_FAILED, e.getMessage(), stagingKey);

		} catch (StorageException e) {
			log.error("Import opId={} storageFailed", operationId, e);
			fail(operationId, ImportStatus.FAILED,
					"Failed to store import file %s".formatted(originalFilename),
					stagingKey
			);

		} catch (Exception e) {
			log.error("Import opId={} failed", operationId, e);
			fail(operationId, ImportStatus.FAILED,
					"Import failed: %s: %s".formatted(e.getClass().getSimpleName(), String.valueOf(e.getMessage())),
					stagingKey
			);
		}
	}

	private void fail(Long operationId, ImportStatus status, String msg, String stagingKey) {
		try {
			TransactionTemplate tt = new TransactionTemplate(txManager);
			tt.executeWithoutResult(txStatus -> {
				ImportOperation op = repository.findById(operationId).orElseThrow(() ->
						new ServiceException(
								BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND,
								"ImportOperation",
								operationId
						)
				);
				op.setStatus(status);
				op.setErrorMessage(msg);
				op.setCreatedEntitiesCount(null);
				op.setCompletedAt(LocalDateTime.now());
				op = repository.save(op);
			});
		} finally {
			cleanup(stagingKey);
		}
	}

	private void cleanup(String stagingKey) {
		if (stagingKey == null) return;
		try {
			storageService.deleteObject(stagingKey);
			log.info("Cleanup staging object {}", stagingKey);
		} catch (Exception e) {
			log.warn("Failed to cleanup staging object {}", stagingKey, e);
		}
	}

	private record DbCommitResult(int createdCount) {}
}
