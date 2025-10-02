package org.is.bandmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "person")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Person.Name не может быть пустым")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Person.EyeColor не может быть пустым")
    @Enumerated(EnumType.STRING)
    @Column(name = "eye_color", nullable = false)
    private Color eyeColor;

    @NotNull(message = "Person.HairColor не может быть пустым")
    @Enumerated(EnumType.STRING)
    @Column(name = "hair_color", nullable = false)
    private Color hairColor;

    @NotNull(message = "Person.Location не может быть пустым")
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @NotNull(message = "Person.Weight не может быть пустым")
    @DecimalMin(value = "0", inclusive = false, message = "Person.Weight должно быть > 0")
    @Column(nullable = false)
    private Float weight;

    @NotNull(message = "Person.Country не может быть пустым")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Country nationality;

}
