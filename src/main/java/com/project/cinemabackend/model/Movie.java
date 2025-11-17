package com.project.cinemabackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "movies", schema = "public")
public class Movie {
    @Id
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "movie_id", nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "original_title", length = 500)
    private String originalTitle;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "age_rating", length = 10)
    private String ageRating;

    @ColumnDefault("'Polish'")
    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "country", length = 100)
    private String country;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive;

    @ColumnDefault("0.00")
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @ColumnDefault("0")
    @Column(name = "rating_count")
    private Integer ratingCount;

    @OneToMany(mappedBy = "movie")
    private Set<Media> media = new LinkedHashSet<>();

    @OneToMany(mappedBy = "movie")
    private Set<MovieActor> movieActors = new LinkedHashSet<>();

    @OneToMany(mappedBy = "movie")
    private Set<MovieDirector> movieDirectors = new LinkedHashSet<>();

    @OneToOne(mappedBy = "movie")
    private MovieEmbedding movieEmbedding;

    @OneToMany(mappedBy = "movie")
    private Set<MovieGenre> movieGenres = new LinkedHashSet<>();

    @OneToMany(mappedBy = "movie")
    private Set<MovieTag> movieTags = new LinkedHashSet<>();

    @OneToMany(mappedBy = "movie")
    private Set<Showtime> showtimes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "movie")
    private Set<UserRating> userRatings = new LinkedHashSet<>();

}