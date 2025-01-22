package io.github.waynem77.bscmail4.model.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * A template for a Shift object.
 */
@Entity
@Table(name = "shift_template")
@Data

public class ShiftTemplate
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name", columnDefinition = "TEXT", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "required_role_id")
    private Role requiredRole;

    public Long getRequiredRoleId()
    {
        return requiredRole != null ?
                requiredRole.getId() :
                null;
    }
}
