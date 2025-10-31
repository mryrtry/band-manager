package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.LocationRequest;
import org.is.bandmanager.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDto toDto(Location location);

    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(LocationRequest request, @MappingTarget Location entity);

}