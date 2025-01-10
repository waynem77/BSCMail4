package io.github.waynem77.bscmail4.model.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * A volunteer role.
 */
@Entity
@Table(name = "role")
@Data
public class Role
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name", columnDefinition = "TEXT", unique = true)
    private String name;
}
