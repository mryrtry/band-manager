package org.is.bandmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "location")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer x;

    @NotNull(message = "Location.Y не может быть пустым")
    @Column(nullable = false)
    private Long y;

    @NotNull(message = "Location.Z не может быть пустым")
    @Column(nullable = false)
    private Long z;

}
