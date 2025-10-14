package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.BestBandAwardRequest;
import org.is.bandmanager.model.BestBandAward;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class BestBandAwardMapper {

    @Mapping(target = "bandId", source = "band.id")
    @Mapping(target = "bandName", source = "band.name")
    public abstract BestBandAwardDto toDto(BestBandAward award);

    @Mapping(target = "band", ignore = true)
    public abstract BestBandAward toEntity(BestBandAwardDto awardDto);

    @Mapping(target = "band", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public BestBandAward toEntity(BestBandAwardRequest request) {
        return BestBandAward.builder()
                .genre(request.getGenre())
                .build();
    }

}