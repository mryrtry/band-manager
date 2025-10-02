package org.is.bandmanager.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDto {

    private Long id;

    private String name;

    private Long tracks;

    private Integer sales;

}