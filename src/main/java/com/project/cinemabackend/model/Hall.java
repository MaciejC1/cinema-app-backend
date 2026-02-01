package com.project.cinemabackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "halls", schema = "public")
public class Hall {
    @Id
    @GeneratedValue
    @Column(name = "hall_id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @ColumnDefault("'standard'")
    @Column(name = "screen_type", length = 50)
    private String screenType;

    @Column(name = "sound_system", length = 50)
    private String soundSystem;

    @ColumnDefault("false")
    @Column(name = "has_3d")
    private Boolean has3d;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "hall")
    private Set<Seat> seats = new LinkedHashSet<>();

    @OneToMany(mappedBy = "hall")
    private Set<Showtime> showtimes = new LinkedHashSet<>();

}