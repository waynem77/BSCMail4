package io.github.waynem77.bscmail4.model.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * A volunteer permission.
 */
@Entity
@Table(name = "permission")
@Data
public class Permission
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name", columnDefinition = "TEXT", unique = true)
    private String name;
}
