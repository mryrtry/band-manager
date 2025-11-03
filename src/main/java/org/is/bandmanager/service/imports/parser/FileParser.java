package org.is.bandmanager.service.imports.parser;

import org.is.bandmanager.dto.importRequest.MusicBandImportRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileParser {

    boolean supports(MultipartFile file);

    List<MusicBandImportRequest> parse(MultipartFile file);

    List<String> getSupportedContentTypes();

    List<String> getSupportedFileExtensions();

}