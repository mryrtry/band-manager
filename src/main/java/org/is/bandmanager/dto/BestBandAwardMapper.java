package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.BestBandAwardRequest;
import org.is.bandmanager.model.BestBandAward;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface BestBandAwardMapper {

    @Mapping(target = "bandId", source = "band.id")
    @Mapping(target = "bandName", source = "band.name")
    BestBandAwardDto toDto(BestBandAward award);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "band", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    BestBandAward toEntity(BestBandAwardRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "band", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(BestBandAwardRequest request, @MappingTarget BestBandAward entity);

}