package com.kostadin.sis.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDateTime expirationTime;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean used = false;

    // For rate limiting - track requests per email
    @Column
    private Integer requestCount = 1;

    @Column
    private LocalDateTime lastRequestAt;

    public PasswordResetToken(String token, String email, LocalDateTime expirationTime) {
        this.token = token;
        this.email = email;
        this.expirationTime = expirationTime;
        this.createdAt = LocalDateTime.now();
        this.lastRequestAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}