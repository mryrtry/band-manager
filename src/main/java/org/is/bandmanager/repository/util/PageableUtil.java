package org.is.bandmanager.repository.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PageableUtil {

	private static final Set<String> ALLOWED_BEST_BAND_AWARD_SORT_FIELDS = Set.of("id", "bandName", "genre", "createdAt", "bandId");

	private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "name", "description", "genre", "numberOfParticipants", "singlesCount", "albumsCount", "establishmentDate", "frontMan.name", "bestAlbum.name", "coordinates.x", "coordinates.y");

	public static Pageable createBestBandAwardPageable(int page, int size, List<String> sort, String direction) {
		List<String> validSortFields = (sort == null ? List.of("createdAt") : sort).stream().filter(ALLOWED_BEST_BAND_AWARD_SORT_FIELDS::contains).collect(Collectors.toList());

		if (validSortFields.isEmpty()) {
			validSortFields = List.of("createdAt");
		}

		Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

		Sort pageSort = Sort.by(sortDirection, validSortFields.toArray(new String[0]));
		return PageRequest.of(page, size, pageSort);
	}

	public static Pageable createMusicBandPageable(int page, int size, List<String> sort, String direction) {
		List<String> validSortFields = (sort == null ? List.of("id") : sort).stream().filter(ALLOWED_SORT_FIELDS::contains).collect(Collectors.toList());

		if (validSortFields.isEmpty()) {
			validSortFields = List.of("id");
		}

		Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

		Sort pageSort = Sort.by(sortDirection, validSortFields.toArray(new String[0]));
		return PageRequest.of(page, size, pageSort);
	}

}