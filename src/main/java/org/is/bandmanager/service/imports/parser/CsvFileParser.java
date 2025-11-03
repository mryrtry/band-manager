package org.is.bandmanager.service.imports.parser;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.RequiredArgsConstructor;
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
public class CsvFileParser implements FileParser {

    private final CsvMapper csvMapper;

    @Override
    public boolean supports(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        return "text/csv".equals(contentType) ||
                "application/vnd.ms-excel".equals(contentType) ||
                (filename != null && filename.toLowerCase().endsWith(".csv"));
    }

    @Override
    public List<MusicBandImportRequest> parse(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            return csvMapper.readerFor(MusicBandImportRequest.class)
                    .with(schema)
                    .<MusicBandImportRequest>readValues(inputStream)
                    .readAll();
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV file", e);
        }
    }

    @Override
    public List<String> getSupportedContentTypes() {
        return Arrays.asList("text/csv", "application/vnd.ms-excel");
    }

}