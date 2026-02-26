package com.shareint.backend.modules.user.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
public class User implements UserDetails {

    @Id
    @GeneratedValue
    private UUID id;

    private String phoneNumber;
    
    private String fullName;
    
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private RoleType role;
    
    private boolean isVerified;

    /** Set to true once NID photo is submitted and verified by OCR + government API. */
    private boolean isNidVerified;

    /** URL to the uploaded NID photo (stored in cloud storage). Used for future OCR verification. */
    private String nidPhotoUrl;

    private String nidNumber;

    private String avatarUrl;

    /** BCrypt-hashed password. Null if user was registered via OTP-only (legacy). */
    private String passwordHash;

    /** Optional email address. Must be unique per user. */
    private String email;

    /** Set to true once user completes the profile setup (name + password + optional email). */
    @Builder.Default
    private boolean isProfileComplete = false;

    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;
    
    private UUID creatorId;
    
    private UUID updaterId;
    
    @Builder.Default
    private Instant createdAt = Instant.now();
    
    @Builder.Default
    private Instant updatedAt = Instant.now();
    
    private Instant deletedAt;

    public enum RoleType {
        PASSENGER, DRIVER, ADMIN
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        // Return the stored BCrypt hash, or empty string for OTP-only (legacy) users
        return passwordHash != null ? passwordHash : "";
    }

    @Override
    public String getUsername() {
        return phoneNumber;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return deletedAt == null;
    }
}
