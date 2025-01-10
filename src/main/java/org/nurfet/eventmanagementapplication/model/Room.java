package org.nurfet.eventmanagementapplication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "rooms")
@AllArgsConstructor
@NoArgsConstructor
public class Room extends AbstractEntity {

    @NotBlank
    private String name;

    @Min(1)
    private int capacity;

    @OneToMany(mappedBy = "room")
    private List<Event> events = new ArrayList<>();

    @Column(nullable = false)
    private boolean deleted = false;
}
