package org.is.bandmanager.service.imports.handler;

public interface ImportHandler {

	void processImport(Long operationId, byte[] fileContent, String fileOriginalFilename, String mimeType, String username);

}
