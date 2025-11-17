package org.is.bandmanager.service.imports.parser;

import org.is.bandmanager.dto.importRequest.MusicBandImportRequest;

import java.util.List;

public interface FileParser {

    boolean supports(String mimeType);

    List<MusicBandImportRequest> parse(byte[] fileContent, String originalFilename);

    List<String> getSupportedContentTypes();

}