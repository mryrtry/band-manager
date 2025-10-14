package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.BestBandAwardRequest;
import org.is.bandmanager.model.BestBandAward;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class BestBandAwardMapper {

    @Mapping(target = "band", source = "band")
    public abstract BestBandAwardDto toDto(BestBandAward award);

    @Mapping(target = "band", source = "band")
    public abstract BestBandAward toEntity(BestBandAwardDto awardDto);

    public BestBandAward toEntity(BestBandAwardRequest request) {
        return BestBandAward.builder()
                .genre(request.getGenre())
                .build();
    }

}