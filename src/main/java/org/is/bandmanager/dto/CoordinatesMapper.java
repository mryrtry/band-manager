package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.CoordinatesRequest;
import org.is.bandmanager.model.Coordinates;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CoordinatesMapper {

    CoordinatesDto toDto(Coordinates coordinates);

    Coordinates toEntity(CoordinatesDto coordinatesDto);

    @Mapping(target = "id", ignore = true)
    Coordinates toEntity(CoordinatesRequest request);

}