package org.is.bandmanager.service.imports.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.importRequest.MusicBandImportRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JsonFileParser implements FileParser {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        return "application/json".equals(contentType) ||
                (filename != null && filename.toLowerCase().endsWith(".json"));
    }

    @Override
    public List<MusicBandImportRequest> parse(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return objectMapper.readValue(inputStream,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MusicBandImportRequest.class));
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON file", e);
        }
    }

    @Override
    public List<String> getSupportedContentTypes() {
        return List.of("application/json");
    }

    @Override
    public List<String> getSupportedFileExtensions() {
        return List.of(".json");
    }

}
