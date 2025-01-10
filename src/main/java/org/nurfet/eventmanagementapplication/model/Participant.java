package org.nurfet.eventmanagementapplication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Participant extends AbstractEntity {

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    @ManyToMany(mappedBy = "participants")
    private Set<Event> events = new HashSet<>();

    @Column(nullable = false)
    private boolean deleted = false;
}
