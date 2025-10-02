package org.is.bandmanager.model.dto;

import org.is.bandmanager.model.Coordinates;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CoordinatesMapper {

    CoordinatesDto toDto(Coordinates coordinates);

    Coordinates toEntity(CoordinatesDto coordinatesDto);

}