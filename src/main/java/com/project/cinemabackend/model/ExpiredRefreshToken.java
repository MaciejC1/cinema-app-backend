package com.project.cinemabackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "expired_refresh_tokens", schema = "public")
public class ExpiredRefreshToken {
    @Id
    @GeneratedValue
    @Column(name = "token_id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token", nullable = false, length = Integer.MAX_VALUE)
    private String refreshToken;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "issued_at", nullable = false)
    private OffsetDateTime issuedAt ;//= OffsetDateTime.now();

    @Column(name = "expired_at", nullable = false)
    private OffsetDateTime expiredAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "invalidated_at")
    private OffsetDateTime invalidatedAt;// = OffsetDateTime.now();

    @ColumnDefault("'expired'")
    @Column(name = "reason")
    private String reason;

}