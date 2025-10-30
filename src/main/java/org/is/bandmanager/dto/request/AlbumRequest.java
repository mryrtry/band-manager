package org.is.bandmanager.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class AlbumRequest {

    @NotBlank(message = "Album.Name не может быть пустым")
    private String name;

    @NotNull(message = "Album.Tracks не может быть пустым")
    @DecimalMin(value = "0", inclusive = false, message = "Album.Tracks должно быть > 0")
    private Long tracks;

    @DecimalMin(value = "0", inclusive = false, message = "Album.Sales должно быть > 0")
    private Integer sales;

}
