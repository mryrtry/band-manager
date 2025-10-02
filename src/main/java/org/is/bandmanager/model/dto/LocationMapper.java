package org.is.bandmanager.model.dto;

import org.is.bandmanager.model.Location;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDto toDto(Location location);

    Location toEntity(LocationDto locationDto);

}