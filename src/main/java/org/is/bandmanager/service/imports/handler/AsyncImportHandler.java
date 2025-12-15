package org.is.bandmanager.service.imports.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.bandmanager.service.imports.orchestrator.ImportOrchestrator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncImportHandler implements ImportHandler {

	private final ImportOrchestrator orchestrator;

	@Override
	@Async("importTaskExecutor")
	public void processImport(Long operationId, byte[] fileContent, String originalFilename, String mimeType, String username) {
		orchestrator.run(operationId, fileContent, originalFilename, mimeType, username);
	}

}

