package org.is.bandmanager.service.imports.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.bandmanager.dto.importRequest.MusicBandImportRequest;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class JsonFileParser implements FileParser {

	private final ObjectMapper objectMapper;

	@Override
	public boolean supports(String mimeType) {
		return "application/json".equals(mimeType);
	}

	@Override
	public List<MusicBandImportRequest> parse(byte[] fileContent, String originalFilename) {
		try (InputStream inputStream = new ByteArrayInputStream(fileContent)) {
			log.debug("Attempting to parse JSON file: {}", originalFilename);
			List<MusicBandImportRequest> result = objectMapper.readValue(inputStream, objectMapper.getTypeFactory().constructCollectionType(List.class, MusicBandImportRequest.class));
			log.debug("Successfully parsed {} MusicBandImportRequest objects from file: {}", result.size(), originalFilename);
			return result;
		} catch (JsonProcessingException e) {
			log.warn("JSON parsing failed for file: {}", originalFilename);
			throw new RuntimeException("JSON parsing failed: " + e.getMessage());
		} catch (IOException e) {
			log.warn("Failed to read or parse file: {}", originalFilename);
			throw new RuntimeException("Failed to read or parse JSON file: " + e.getMessage());
		}
	}

	@Override
	public List<String> getSupportedContentTypes() {
		return List.of("application/json");
	}

}