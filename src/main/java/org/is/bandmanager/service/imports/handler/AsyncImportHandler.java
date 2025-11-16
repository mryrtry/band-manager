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

	@Override
	@Async("importTaskExecutor")
	public void processImport(Long operationId, byte[] fileContent, String originalFilename, String mimeType) {
		ImportOperation operation = repository.findById(operationId)
				.orElseThrow(() -> new ServiceException(BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND, "ImportOperation", operationId));
		try {
			log.info("Starting import processing for operation {}: {}", operation.getId(), originalFilename);
			operation.setStatus(ImportStatus.PROCESSING);
			operation = repository.save(operation);
			List<MusicBandImportRequest> importRequests = fileParserFacade.parseFile(fileContent, originalFilename, mimeType);
			log.info("Parsed {} records from file {}", importRequests.size(), originalFilename);
			if (importRequests.isEmpty()) {
				throw new IllegalArgumentException("Import file is empty");
			}
			List<Long> createdBandIds = processor.processImport(importRequests);
			operation.setStatus(ImportStatus.COMPLETED);
			operation.setCreatedEntitiesCount(createdBandIds.size());
			operation.setCompletedAt(LocalDateTime.now());
			log.info("Import operation {} completed successfully. Created {} MusicBand entities",
					operation.getId(), createdBandIds.size());
		} catch (ValidationException e) {
			operation.setStatus(ImportStatus.VALIDATION_FAILED);
			operation.setErrorMessage(getErrorMessage(e));
		} catch (Exception e) {
			operation.setStatus(ImportStatus.FAILED);
			operation.setErrorMessage(getErrorMessage(e));
		} finally {
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

}
