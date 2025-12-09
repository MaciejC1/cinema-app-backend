package com.project.cinemabackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "showtimes", schema = "public")
public class Showtime {
    @Id
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "showtime_id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private OffsetDateTime endTime;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "vip_price", precision = 10, scale = 2)
    private BigDecimal vipPrice;

    @Column(name = "premium_price", precision = 10, scale = 2)
    private BigDecimal premiumPrice;

    @ColumnDefault("false")
    @Column(name = "is_3d")
    private Boolean is3d;

    @ColumnDefault("'Polish'")
    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "has_subtitles", nullable = false)
    @ColumnDefault("false")
    private Boolean hasSubtitles;

    @Enumerated(EnumType.STRING)
    @Column(name = "audio_track", length = 20, nullable = false)
    private AudioTrackType audioTrack;

    @Column(name = "subtitles", length = 50)
    private String subtitles;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "showtime")
    private Set<BookingSeat> bookingSeats = new LinkedHashSet<>();

}