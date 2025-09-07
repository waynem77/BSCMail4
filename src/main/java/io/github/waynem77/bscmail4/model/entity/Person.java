package io.github.waynem77.bscmail4.model.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Represents a volunteer in the system.
 */
@Entity
@Table(name = "person")
@Data
public class Person
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email_address", nullable = false)
    private String emailAddress;

    @Column(name = "phone")
    private String phone;

    @Column(name = "active", nullable = false)
    private Boolean active;
}