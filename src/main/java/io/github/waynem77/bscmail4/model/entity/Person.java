package io.github.waynem77.bscmail4.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

/**
 * A volunteer
 */
@Entity
@Table(name = "person")
@Data
public class Person
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name", columnDefinition = "TEXT", nullable = false)
    private String name;

    @Column(name = "email_address", columnDefinition = "TEXT", nullable = false)
    private String emailAddress;

    @Column(name = "phone", columnDefinition = "TEXT")
    private String phone;

    @Column(name = "active", columnDefinition = "BOOLEAN", nullable = false)
    private Boolean active;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable
    private Set<Permission> permissions;
}
