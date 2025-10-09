package org.is.bandmanager.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.exception.ServiceException;
import org.is.bandmanager.model.Location;
import org.is.bandmanager.dto.LocationDto;
import org.is.bandmanager.dto.LocationMapper;
import org.is.bandmanager.dto.request.LocationRequest;
import org.is.bandmanager.repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.is.bandmanager.exception.message.ServiceErrorMessage.MUST_BE_NOT_NULL;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.SOURCE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper mapper;

    private Location toEntity(LocationRequest request) {
        return Location.builder()
                .x(request.getX())
                .y(request.getY())
                .z(request.getZ())
                .build();
    }

    private Location findLocationById(Long id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "Location.id");
        }
        return locationRepository.findById(id)
                .orElseThrow(() -> new ServiceException(SOURCE_NOT_FOUND, "Location", id));
    }

    public LocationDto createLocation(@Valid LocationRequest request) {
        Location location = locationRepository.save(toEntity(request));
        return mapper.toDto(location);
    }

    public List<LocationDto> getLocations() {
        return locationRepository.findAll().stream().map(mapper::toDto).toList();
    }

    public LocationDto getLocation(Long id) {
        return mapper.toDto(findLocationById(id));
    }

    public LocationDto updateLocation(Long id, @Valid LocationRequest request) {
        findLocationById(id);
        Location updatedLocation = toEntity(request);
        updatedLocation.setId(id);
        return mapper.toDto(locationRepository.save(updatedLocation));
    }

    public LocationDto deleteLocation(Long id) {
        Location location = findLocationById(id);
        locationRepository.delete(location);
        return mapper.toDto(location);
    }

}