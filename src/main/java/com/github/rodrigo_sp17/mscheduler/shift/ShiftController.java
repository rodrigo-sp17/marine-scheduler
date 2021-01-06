package com.github.rodrigo_sp17.mscheduler.shift;

import com.github.rodrigo_sp17.mscheduler.shift.data.Shift;
import com.github.rodrigo_sp17.mscheduler.shift.data.ShiftRequest;
import com.github.rodrigo_sp17.mscheduler.user.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/shift")
public class ShiftController {

    @Autowired
    private final ShiftService shiftService;

    @Autowired
    private final UserService userService;

    public ShiftController(ShiftService shiftService, UserService userService) {
        this.shiftService = shiftService;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shift> getShiftById(@PathVariable Long id,
                                              Authentication auth) {
        Shift result = shiftService.getShiftById(id, auth.getName());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<Shift>> getShifts(Authentication auth) {
        List<Shift> results = shiftService.getShiftsForUser(auth.getName());
        return ResponseEntity.ok(results);
    }

    @PostMapping("/add")
    public ResponseEntity<List<Shift>> addShift(@RequestBody ShiftRequest shiftRequest,
                                          Authentication auth) {
        String errorMsg;
        // Ensures a boarding date
        if (shiftRequest.getBoardingDate() == null) {
            errorMsg = "BoardingDate cannot be null";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }

        // Adjusts cycle, if not provided a leaving date
        // Ensures a leaving date
        if (shiftRequest.getLeavingDate() == null) {
            if (shiftRequest.getCycleDays() > 0) {
                var boardingDate = shiftRequest.getBoardingDate();
                var leavingDate = boardingDate.plusDays(shiftRequest.getCycleDays());
                shiftRequest.setLeavingDate(leavingDate);
                shiftRequest.setUnavailabilityEndDate(leavingDate);
            } else {
                errorMsg = "LeavingDate and CycleDays can't be both null";
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
            }
        }

        // Ensures unavailability dates
        ShiftRequest req = sanitizeRequest(shiftRequest);

        // Validates dates
        if (!validateRequestDates(req)) {
            errorMsg = "Period of unavailability cannot be smaller than boarding period";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }

        // Ensures repeat field is not null
        Integer repeat = req.getRepeat();
        if (repeat == null) {
            req.setRepeat(0);
        }

        // Creates the necessary number of shifts according to the the repeat parameter
        long cycleDays = ChronoUnit.DAYS.between(req.getBoardingDate(),
                req.getLeavingDate());
        long beforeDiff = ChronoUnit.DAYS.between(req.getUnavailabilityStartDate(),
                req.getBoardingDate());
        long afterDiff = ChronoUnit.DAYS.between(req.getLeavingDate(),
                req.getUnavailabilityEndDate());

        List<Shift> shifts = new ArrayList<>();
        for (int i = 0; i <= repeat; i++) {
            Shift shift = new Shift();
            shift.setBoardingDate(
                    req.getBoardingDate()
                            .plusDays(i * 2 * cycleDays));
            shift.setLeavingDate(req.getLeavingDate()
                    .plusDays(i * 2 * cycleDays));
            shift.setUnavailabilityStartDate(
                    shift.getBoardingDate().minusDays(beforeDiff));
            shift.setUnavailabilityEndDate(
                    shift.getLeavingDate().minusDays(afterDiff));
            shifts.add(shift);
        }

        // Saves shifts
        List<Shift> result = shiftService.addShifts(shifts, auth.getName());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/edit")
    public ResponseEntity<Shift> editShift(@RequestBody ShiftRequest shiftRequest,
                                           Authentication auth) {
        String errorMsg;
        Long shiftId = shiftRequest.getShiftId();
        if (shiftId == null) {
            errorMsg = "ShiftId can't be null when editing";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }

        Shift shiftToEdit = shiftService.getShiftById(shiftId, auth.getName());
        if (shiftToEdit == null) {
            errorMsg = "You are not authorized to edit this shift!";
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, errorMsg);
        }

        ShiftRequest req = sanitizeRequest(shiftRequest);
        if (!validateRequestDates(req)) {
            errorMsg = "Period of unavailability cannot be smaller than boarding period";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }

        // Parses from request
        shiftToEdit.setUnavailabilityStartDate(req.getUnavailabilityStartDate());
        shiftToEdit.setBoardingDate(req.getBoardingDate());
        shiftToEdit.setLeavingDate(req.getLeavingDate());
        shiftToEdit.setUnavailabilityEndDate(req.getUnavailabilityEndDate());

        Shift result = shiftService.editShift(shiftToEdit);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Shift> removeShift(@RequestParam Long id,
                                             Authentication auth) {
        String errorMsg;
        Shift shiftToDelete = shiftService.getShiftById(id, auth.getName());
        if (shiftToDelete == null) {
            errorMsg = "Either you are not the owner, or the shift can't be found!";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }
        shiftService.removeShift(shiftToDelete.getShiftId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Checks if the dates inside the request make sense
     * @return  true if valid, false if invalid
     */
    private boolean validateRequestDates(ShiftRequest req) {
        if (req.getUnavailabilityStartDate() != null
                && req.getUnavailabilityStartDate().isAfter(req.getBoardingDate())) {
            return false;
        }
        if (req.getBoardingDate() == null || req.getLeavingDate() == null) {
            return false;
        }
        if (req.getBoardingDate().isAfter(req.getLeavingDate())) {
            return false;
        }
        if (req.getUnavailabilityEndDate() != null
                && req.getUnavailabilityEndDate().isBefore(req.getLeavingDate())) {
            return false;
        }
        return true;
    }

    /**
     * Sanitizes ShiftRequest to ensure all date fields are present
     * @param request the request to be sanitized
     * @return sanitized request with all dates filled
     */
    private ShiftRequest sanitizeRequest(ShiftRequest request) {
        if (request.getUnavailabilityStartDate() == null) {
            request.setUnavailabilityStartDate(request.getBoardingDate());
        }
        if (request.getUnavailabilityEndDate() == null) {
            request.setUnavailabilityEndDate(request.getLeavingDate());
        }
        return request;
    }
}
