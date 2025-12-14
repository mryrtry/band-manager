package org.is.bandmanager.service.imports.processor;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.bandmanager.dto.importRequest.AlbumImportRequest;
import org.is.bandmanager.dto.importRequest.CoordinatesImportRequest;
import org.is.bandmanager.dto.importRequest.LocationImportRequest;
import org.is.bandmanager.dto.importRequest.MusicBandImportRequest;
import org.is.bandmanager.dto.importRequest.PersonImportRequest;
import org.is.bandmanager.model.Album;
import org.is.bandmanager.model.Coordinates;
import org.is.bandmanager.model.Location;
import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.model.Person;
import org.is.bandmanager.repository.AlbumRepository;
import org.is.bandmanager.repository.CoordinatesRepository;
import org.is.bandmanager.repository.LocationRepository;
import org.is.bandmanager.repository.MusicBandRepository;
import org.is.bandmanager.repository.PersonRepository;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.SmartValidator;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class MusicBandImportProcessor {

	private final MusicBandRepository musicBandRepository;

	private final CoordinatesRepository coordinatesRepository;

	private final AlbumRepository albumRepository;

	private final PersonRepository personRepository;

	private final LocationRepository locationRepository;

	private final SmartValidator validator;

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public List<Long> processImport(List<MusicBandImportRequest> importRequests, String username) {
		log.info("Processing music band import records={} user={}", importRequests.size(), username);
		List<Long> createdBandIds = new ArrayList<>();
		for (int i = 0; i < importRequests.size(); i++) {
			MusicBandImportRequest request = importRequests.get(i);
			String validationError = validateImportRequest(request);
			if (!validationError.isEmpty()) {
				log.warn("Validation failed for record {}: {}", i + 1, validationError);
				throw new ValidationException("Validation failed for record " + (i + 1) + ": " + validationError); // Исключение для отката транзакции
			}
			try {
				MusicBand musicBand = createMusicBandFromImport(request);
				musicBand.setCreatedBy(username);
				musicBand.setLastModifiedBy(username);
				MusicBand savedBand = musicBandRepository.save(musicBand);
				createdBandIds.add(savedBand.getId());
				log.debug("Created music band from import idx={} id={}", i, savedBand.getId());
			} catch (PessimisticLockingFailureException e) {
				log.warn("Serialization/deadlock issue at import record {}. Will retry. Cause: {}", i + 1, e.getMessage());
				throw e;
			} catch (Exception e) {
				log.error("Failed to process import request at index {}: {}", i + 1, e.getMessage());
				throw new RuntimeException("Import failed at record " + (i + 1) + ": " + e.getMessage(), e); // Исключение для отката транзакции
			}
		}
		log.info("Completed music band import created={}", createdBandIds.size());
		return createdBandIds;
	}

	private String validateImportRequest(MusicBandImportRequest request) {
		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(request, "importRequest");
		validator.validate(request, errors);
		if (errors.hasErrors()) {
			return errors.getFieldErrors().stream().findFirst().map(DefaultMessageSourceResolvable::getDefaultMessage).orElse("Validation failed");
		}
		return "";
	}

	private MusicBand createMusicBandFromImport(MusicBandImportRequest request) {
		Location location = createLocation(request.getFrontMan().getLocation());
		Person frontMan = createPerson(request.getFrontMan(), location);
		Coordinates coordinates = createCoordinates(request.getCoordinates());
		Album bestAlbum = createAlbum(request.getBestAlbum());
		return MusicBand.builder().name(request.getName()).coordinates(coordinates).genre(request.getGenre()).numberOfParticipants(request.getNumberOfParticipants()).singlesCount(request.getSinglesCount()).description(request.getDescription()).bestAlbum(bestAlbum).albumsCount(request.getAlbumsCount()).establishmentDate(request.getEstablishmentDate()).frontMan(frontMan).build();
	}

	private Location createLocation(LocationImportRequest locationRequest) {
		Location location = Location.builder().x(locationRequest.getX()).y(locationRequest.getY()).z(locationRequest.getZ()).build();
		return locationRepository.save(location);
	}

	private Person createPerson(PersonImportRequest personRequest, Location location) {
		Person person = Person.builder().name(personRequest.getName()).eyeColor(personRequest.getEyeColor()).hairColor(personRequest.getHairColor()).location(location).weight(personRequest.getWeight()).nationality(personRequest.getNationality()).build();
		return personRepository.save(person);
	}

	private Coordinates createCoordinates(CoordinatesImportRequest coordinatesRequest) {
		Coordinates coordinates = Coordinates.builder().x(coordinatesRequest.getX()).y(coordinatesRequest.getY()).build();
		return coordinatesRepository.save(coordinates);
	}

	private Album createAlbum(AlbumImportRequest albumRequest) {
		Album album = Album.builder().name(albumRequest.getName()).tracks(albumRequest.getTracks()).sales(albumRequest.getSales()).build();
		return albumRepository.save(album);
	}

}
