package com.github.rodrigo_sp17.mscheduler.user.data;

import com.github.rodrigo_sp17.mscheduler.shift.data.Shift;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@EqualsAndHashCode
public class AppUser {

    @Id
    @GeneratedValue
    private Long userId;

    @Embedded
    private UserInfo userInfo;

    @ManyToMany
    List<AppUser> friends;

    @OneToMany(mappedBy = "owner")
    private List<Shift> shifts;


    public AppUser() {
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public List<AppUser> getFriends() {
        return friends;
    }

    public void setFriends(List<AppUser> friends) {
        this.friends = friends;
    }

    public List<Shift> getShifts() {
        return shifts;
    }

    public void setShifts(List<Shift> shifts) {
        this.shifts = shifts;
    }

    @Override
    public String toString() {
        return (userId + ", " + userInfo.toString());
    }
}
