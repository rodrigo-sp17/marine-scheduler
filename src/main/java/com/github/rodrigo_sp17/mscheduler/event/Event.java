package com.github.rodrigo_sp17.mscheduler.event;

import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Event {
    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean allDay;

    @ManyToOne
    private AppUser owner;
    @ManyToMany
    private List<AppUser> invited;
    @ManyToMany
    private List<AppUser> confirmed;
 }
