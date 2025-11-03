package org.is.bandmanager.service.imports.parser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.is.bandmanager.dto.importRequest.MusicBandImportRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class XmlFileParser implements FileParser {

    private final XmlMapper xmlMapper;

    @Override
    public boolean supports(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        return "application/xml".equals(contentType) ||
                "text/xml".equals(contentType) ||
                (filename != null && filename.toLowerCase().endsWith(".xml"));
    }

    @Override
    public List<MusicBandImportRequest> parse(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            // Для XML нужно будет определить структуру - временная реализация
            MusicBandImportListWrapper wrapper = xmlMapper.readValue(inputStream, MusicBandImportListWrapper.class);
            return wrapper.getMusicBands();
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse XML file", e);
        }
    }

    @Override
    public List<String> getSupportedContentTypes() {
        return Arrays.asList("application/xml", "text/xml");
    }

    @Setter
    @Getter
    public static class MusicBandImportListWrapper {
        private List<MusicBandImportRequest> musicBands;

    }

}