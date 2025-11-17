package org.is.bandmanager.service.imports.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.bandmanager.dto.importRequest.MusicBandImportRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileParserFacade {

    private final List<FileParser> parsers;

    public List<MusicBandImportRequest> parseFile(byte[] fileContent, String originalFilename, String mimeType) {
        FileParser parser = parsers.stream()
                .filter(p -> p.supports(mimeType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported file format. Supported formats: " + getSupportedFormats()));
        log.info("Using parser: {} for file: {}", parser.getClass().getSimpleName(), originalFilename);
        return parser.parse(fileContent, originalFilename);
    }

    public List<String> getSupportedFormats() {
        return parsers.stream()
                .flatMap(parser -> parser.getSupportedContentTypes().stream())
                .toList();
    }

}