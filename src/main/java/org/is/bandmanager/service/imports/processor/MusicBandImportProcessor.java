package org.is.bandmanager.service.imports.processor;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
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
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.SmartValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final Validator jakartaValidator;

    public List<Long> processImport(List<MusicBandImportRequest> importRequests) {
        List<Long> createdBandIds = new ArrayList<>();
        for (int i = 0; i < importRequests.size(); i++) {
            MusicBandImportRequest request = importRequests.get(i);
            try {
                String errorMessage = validateImportRequest(request);
                if (!errorMessage.isBlank()) {
                    log.warn("Validation error at record {}: {}", i + 1, errorMessage);
                    throw new ValidationException(errorMessage);
                }
                MusicBand musicBand = createMusicBandFromImport(request);
                MusicBand savedBand = musicBandRepository.save(musicBand);
                createdBandIds.add(savedBand.getId());
                log.debug("Successfully created MusicBand from import request at index {}", i);
            } catch (ValidationException e) {
                log.error("Validation error at record {}: {}", i + 1, e.getMessage());
                throw new ValidationException(e.getMessage());
            } catch (Exception e) {
                log.error("Failed to process import request at index {} because of {}", i, e.getMessage());
                String finalMessage = extractValidationMessage(e.getMessage());
                throw new RuntimeException("Import failed at record " + (i + 1) + ". Error: " + finalMessage);
            }
        }
        return createdBandIds;
    }

    private String extractValidationMessage(String fullMessage) {
        if (fullMessage == null) {
            return "Unknown error";
        }
        Pattern pattern = Pattern.compile("interpolatedMessage='([^']+)'");
        Matcher matcher = pattern.matcher(fullMessage);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return fullMessage;
    }

    private String validateImportRequest(MusicBandImportRequest request) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(request, "importRequest");
        validator.validate(request, errors);
        if (errors.hasErrors()) {
            return errors.getFieldErrors().stream()
                    .findFirst()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .orElse("Validation failed");
        }
        String nestedValidationMessage = validateNestedEntities(request);
        if (!nestedValidationMessage.isEmpty()) {
            return nestedValidationMessage;
        }

        return "";
    }

    private String validateNestedEntities(MusicBandImportRequest request) {
        if (request.getCoordinates() != null) {
            Set<ConstraintViolation<CoordinatesImportRequest>> coordinateViolations = jakartaValidator.validate(request.getCoordinates());
            if (!coordinateViolations.isEmpty()) {
                return coordinateViolations.iterator().next().getMessage();
            }
        }
        if (request.getFrontMan() != null) {
            Set<ConstraintViolation<PersonImportRequest>> personViolations = jakartaValidator.validate(request.getFrontMan());
            if (!personViolations.isEmpty()) {
                return personViolations.iterator().next().getMessage();
            }
            if (request.getFrontMan().getLocation() != null) {
                Set<ConstraintViolation<LocationImportRequest>> locationViolations = jakartaValidator.validate(request.getFrontMan().getLocation());
                if (!locationViolations.isEmpty()) {
                    return locationViolations.iterator().next().getMessage();
                }
            }
        }
        if (request.getBestAlbum() != null) {
            Set<ConstraintViolation<AlbumImportRequest>> albumViolations = jakartaValidator.validate(request.getBestAlbum());
            if (!albumViolations.isEmpty()) {
                return albumViolations.iterator().next().getMessage();
            }
        }
        return "";
    }

    private MusicBand createMusicBandFromImport(MusicBandImportRequest request) {
        Location location = createLocation(request.getFrontMan().getLocation());
        Person frontMan = createPerson(request.getFrontMan(), location);
        Coordinates coordinates = createCoordinates(request.getCoordinates());
        Album bestAlbum = createAlbum(request.getBestAlbum());

        return MusicBand.builder()
                .name(request.getName())
                .coordinates(coordinates)
                .genre(request.getGenre())
                .numberOfParticipants(request.getNumberOfParticipants())
                .singlesCount(request.getSinglesCount())
                .description(request.getDescription())
                .bestAlbum(bestAlbum)
                .albumsCount(request.getAlbumsCount())
                .establishmentDate(request.getEstablishmentDate())
                .frontMan(frontMan)
                .build();
    }

    private Location createLocation(LocationImportRequest locationRequest) {
        Location location = Location.builder()
                .x(locationRequest.getX())
                .y(locationRequest.getY())
                .z(locationRequest.getZ())
                .build();
        return locationRepository.save(location);
    }

    private Person createPerson(PersonImportRequest personRequest, Location location) {
        Person person = Person.builder()
                .name(personRequest.getName())
                .eyeColor(personRequest.getEyeColor())
                .hairColor(personRequest.getHairColor())
                .location(location)
                .weight(personRequest.getWeight())
                .nationality(personRequest.getNationality())
                .build();
        return personRepository.save(person);
    }

    private Coordinates createCoordinates(CoordinatesImportRequest coordinatesRequest) {
        Coordinates coordinates = Coordinates.builder()
                .x(coordinatesRequest.getX())
                .y(coordinatesRequest.getY())
                .build();
        return coordinatesRepository.save(coordinates);
    }

    private Album createAlbum(AlbumImportRequest albumRequest) {
        Album album = Album.builder()
                .name(albumRequest.getName())
                .tracks(albumRequest.getTracks())
                .sales(albumRequest.getSales())
                .build();
        return albumRepository.save(album);
    }

}