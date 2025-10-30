package org.is.bandmanager.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CoordinatesRequest {

    @NotNull(message = "Coordinates.X не может быть пустым")
    @DecimalMin(value = "-147", inclusive = false, message = "Coordinates.X должно быть больше -147")
    private Integer x;

    private Float y;

}
