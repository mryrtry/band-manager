package org.is.bandmanager.service.location;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.LocationDto;
import org.is.bandmanager.dto.LocationMapper;
import org.is.bandmanager.dto.request.LocationRequest;
import org.is.event.EntityEvent;
import org.is.exception.ServiceException;
import org.is.bandmanager.model.Location;
import org.is.bandmanager.repository.LocationRepository;
import org.is.bandmanager.repository.PersonRepository;
import org.is.bandmanager.service.cleanup.CleanupStrategy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static org.is.event.EventType.CREATED;
import static org.is.event.EventType.DELETED;
import static org.is.event.EventType.UPDATED;
import static org.is.bandmanager.exception.message.BandManagerErrorMessage.ENTITY_IN_USE;
import static org.is.bandmanager.exception.message.BandManagerErrorMessage.ID_MUST_BE_POSITIVE;
import static org.is.bandmanager.exception.message.BandManagerErrorMessage.MUST_BE_NOT_NULL;
import static org.is.bandmanager.exception.message.BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND;


@Service
@Validated
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService, CleanupStrategy<Location, LocationDto> {

    private final LocationRepository locationRepository;

    private final PersonRepository personRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final LocationMapper mapper;

    private Location findById(Long id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "Location.id");
        }
        if (id <= 0) {
            throw new ServiceException(ID_MUST_BE_POSITIVE, "Location.id");
        }
        return locationRepository.findById(id).orElseThrow(() -> new ServiceException(SOURCE_WITH_ID_NOT_FOUND, "Location", id));
    }

    @Transactional
    protected void checkDependencies(Location location) {
        Long locationId = location.getId();
        if (personRepository.existsByLocationId(locationId)) {
            throw new ServiceException(ENTITY_IN_USE, "Location", locationId, "Person");
        }
    }

    @Override
    @Transactional
    public LocationDto create(LocationRequest request) {
        Location location = mapper.toEntity(request);
        LocationDto createdLocation = mapper.toDto(locationRepository.save(location));
        eventPublisher.publishEvent(new EntityEvent<>(CREATED, createdLocation));
        return createdLocation;
    }

    @Override
    public List<LocationDto> getAll() {
        return locationRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public LocationDto get(Long id) {
        return mapper.toDto(findById(id));
    }

    @Override
    public Location getEntity(Long id) {
        return findById(id);
    }

    @Override
    @Transactional
    public LocationDto update(Long id, LocationRequest request) {
        Location updatingLocation = findById(id);
        mapper.updateEntityFromRequest(request, updatingLocation);
        LocationDto updatedLocation = mapper.toDto(locationRepository.save(updatingLocation));
        eventPublisher.publishEvent(new EntityEvent<>(UPDATED, updatedLocation));
        return updatedLocation;
    }

    @Override
    @Transactional
    public LocationDto delete(Long id) {
        Location location = findById(id);
        checkDependencies(location);
        locationRepository.delete(location);
        LocationDto deletedLocation = mapper.toDto(location);
        eventPublisher.publishEvent(new EntityEvent<>(DELETED, deletedLocation));
        return deletedLocation;
    }

    @Override
    public List<Location> findUnusedEntities() {
        return locationRepository.findUnusedLocations();
    }

    @Override
    public void deleteEntities(List<Location> entities) {
        locationRepository.deleteAll(entities);
    }

    @Override
    public List<LocationDto> convertToDto(List<Location> entities) {
        return entities.stream().map(mapper::toDto).toList();
    }

    @Override
    public String getEntityName() {
        return "Location";
    }

}