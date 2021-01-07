package com.github.rodrigo_sp17.mscheduler.calendar;

import com.github.rodrigo_sp17.mscheduler.friend.FriendService;
import com.github.rodrigo_sp17.mscheduler.shift.ShiftService;
import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    @Autowired
    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<AppUser>> getAvailableFriends(@RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                             Authentication auth) {
        var availableFriends = calendarService.getAvailableFriends(date,
                auth.getName());
        return ResponseEntity.ok(availableFriends);
    }
}
