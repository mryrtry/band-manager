package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.CoordinatesRequest;
import org.is.bandmanager.model.Coordinates;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface CoordinatesMapper {

    CoordinatesDto toDto(Coordinates coordinates);

    @Mapping(target = "id", ignore = true)
    Coordinates toEntity(CoordinatesRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(CoordinatesRequest request, @MappingTarget Coordinates entity);

}