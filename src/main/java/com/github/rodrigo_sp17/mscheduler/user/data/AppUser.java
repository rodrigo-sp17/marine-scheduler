package com.github.rodrigo_sp17.mscheduler.user.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.rodrigo_sp17.mscheduler.event.Event;
import com.github.rodrigo_sp17.mscheduler.shift.data.Shift;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class AppUser extends RepresentationModel<AppUser> {

    @Id
    @GeneratedValue
    private Long userId;

    @Embedded
    private UserInfo userInfo;

    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.DETACH})
    @JsonIgnore
    private List<AppUser> friends;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE)
    private List<Shift> shifts;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE)
    private List<Event> ownedEvents;
}
