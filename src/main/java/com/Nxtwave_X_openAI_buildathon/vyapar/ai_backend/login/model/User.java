package com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * User entity with OAuth2 support (Trivana-style).
 * - email is unique identifier
 * - username for compatibility (same as email)
 * - authProvider: LOCAL or GOOGLE
 * - googleId: Google's unique user ID (null for local users)
 * - enabled: email verification status
 */
@Entity
@Table(name = "Vyapar_USERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email; // Primary identifier (unique)

    @Column(nullable = false)
    private String password; // BCrypt hashed password

    private String username; // For compatibility (usually same as email)

    private String name; // Display name / Full name

    @Column(name = "auth_provider")
    private String authProvider; // "LOCAL" or "GOOGLE"

    @Column(name = "google_id")
    private String googleId; // Google user ID for OAuth users

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private boolean enabled = false; // Email verification status

    // Backward compatibility getters/setters
    public String getFullName() { return name; }
    public void setFullName(String name) { this.name = name; }
    
    public String getProvider() { return authProvider; }
    public void setProvider(String provider) { this.authProvider = provider; }
    
    public String getProviderId() { return googleId; }
    public void setProviderId(String id) { this.googleId = id; }
}
