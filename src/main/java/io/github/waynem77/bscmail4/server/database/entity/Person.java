package io.github.waynem77.bscmail4.server.database.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * A Person represents an individual in our system.
 */
@Entity
@Table(name = "person")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person
{
    /**
     * Unique identifier for the person.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private Long id;

    /**
     * The person's full name.
     */
    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * The person's email address.
     */
    @NotBlank
    @Column(name = "email_address", nullable = false)
    private String emailAddress;

    /**
     * The person's phone number (optional).
     */
    @Column(name = "phone")
    private String phone;

    /**
     * Whether the person is currently active in the system.
     */
    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /**
     * The timestamp when the person was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

