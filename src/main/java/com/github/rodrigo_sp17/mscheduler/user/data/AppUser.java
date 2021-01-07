package com.github.rodrigo_sp17.mscheduler.user.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.rodrigo_sp17.mscheduler.friend.FriendRequest;
import com.github.rodrigo_sp17.mscheduler.shift.data.Shift;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class AppUser {

    @Id
    @GeneratedValue
    private Long userId;

    @Embedded
    private UserInfo userInfo;

    @ManyToMany
    @JsonIgnore
    private List<AppUser> friends;

    @OneToMany(mappedBy = "owner")
    private List<Shift> shifts;
}
