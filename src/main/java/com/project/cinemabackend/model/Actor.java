package com.project.cinemabackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "actors", schema = "public")
public class Actor {
    @Id
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "actor_id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "biography", length = Integer.MAX_VALUE)
    private String biography;

    @Column(name = "photo_url", length = 1000)
    private String photoUrl;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "actor")
    private Set<MovieActor> movieActors = new LinkedHashSet<>();

}