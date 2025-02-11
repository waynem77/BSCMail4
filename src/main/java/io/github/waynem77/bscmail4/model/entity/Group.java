package io.github.waynem77.bscmail4.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A group of volunteers.
 */
@Entity
// The table name is not a typo. "Group" is a reserved word in SQL, and is challenging to uses as a table name.
@Table(name = "groupp")
@Data
public class Group
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @Column(name = "name", columnDefinition = "TEXT", nullable = false, unique = true)
    private String name;

    @Transient
    private List<Person> people;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "group_permission",
            joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions;
}
