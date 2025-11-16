package org.is.bandmanager.service.imports.parser;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.is.bandmanager.dto.importRequest.MusicBandImportRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;


@Component
@RequiredArgsConstructor
public class XmlFileParser implements FileParser {

	@Override
	public boolean supports(MultipartFile file) {
		String contentType = file.getContentType();
		return "application/xml".equals(contentType) || "text/xml".equals(contentType);
	}

	@Override
	public List<MusicBandImportRequest> parse(MultipartFile file) {
		XStream xstream = new XStream();
		xstream.addPermission(com.thoughtworks.xstream.security.AnyTypePermission.ANY);
		xstream.alias("musicBand", MusicBandImportRequest.class);
		xstream.alias("coordinates", org.is.bandmanager.dto.importRequest.CoordinatesImportRequest.class);
		xstream.alias("album", org.is.bandmanager.dto.importRequest.AlbumImportRequest.class);
		xstream.alias("person", org.is.bandmanager.dto.importRequest.PersonImportRequest.class);
		xstream.alias("location", org.is.bandmanager.dto.importRequest.LocationImportRequest.class);
		xstream.alias("musicBands", XStreamWrapper.class);
		xstream.addImplicitCollection(XStreamWrapper.class, "musicBandsList", "musicBand", MusicBandImportRequest.class);
		xstream.registerConverter(new NullableIntegerConverter());
		xstream.registerConverter(new NullableFloatConverter());
		try (InputStream inputStream = file.getInputStream()) {
			XStreamWrapper wrapper = (XStreamWrapper) xstream.fromXML(inputStream);
			return wrapper.getMusicBandsList();
		} catch (Exception e) {
			throw new RuntimeException("Failed to read XML file");
		}
	}

	@Override
	public List<String> getSupportedContentTypes() {
		return List.of("application/xml", "text/xml");
	}

	@Setter
	@Getter
	public static class XStreamWrapper {

		private List<MusicBandImportRequest> musicBandsList;

	}

	public static class NullableIntegerConverter implements Converter {

		@Override
		public boolean canConvert(Class type) {
			return type == Integer.class || type == int.class;
		}

		@Override
		public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
			if (source != null) {
				writer.setValue(source.toString());
			}
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
			String value = reader.getValue();
			if (value == null || value.trim().isEmpty()) {
				return null;
			}
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new RuntimeException("Failed to parse Integer from value: " + value);
			}
		}

	}

	public static class NullableFloatConverter implements Converter {

		@Override
		public boolean canConvert(Class type) {
			return type == Float.class || type == float.class;
		}

		@Override
		public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
			if (source != null) {
				writer.setValue(source.toString());
			}
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
			String value = reader.getValue();
			if (value == null || value.trim().isEmpty()) {
				return null;
			}
			try {
				return Float.parseFloat(value);
			} catch (NumberFormatException e) {
				throw new RuntimeException("Failed to parse Float from value: " + value);
			}
		}

	}

}