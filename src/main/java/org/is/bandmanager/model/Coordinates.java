package org.is.bandmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "coordinates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coordinates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Coordinates.X не может быть пустым")
    @DecimalMin(value = "-147", inclusive = false, message = "Coordinates.X должно быть больше -147")
    @Column(nullable = false)
    private Integer x;

    private Float y;

}
