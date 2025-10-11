package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.LocationRequest;
import org.is.bandmanager.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);

    LocationDto toDto(Location location);

    Location toEntity(LocationDto locationDto);

    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationRequest request);

}