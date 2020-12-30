package com.github.rodrigo_sp17.mscheduler.shift;

import com.github.rodrigo_sp17.mscheduler.shift.data.Shift;
import com.github.rodrigo_sp17.mscheduler.shift.data.ShiftRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    @Autowired
    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    public ResponseEntity<List<Shift>> getShifts(Authentication auth) {
        throw new UnsupportedOperationException();
    }

    @PostMapping("/add")
    public ResponseEntity<Shift> addShift(ShiftRequest shiftRequest,
                                          Authentication auth) {
        throw new UnsupportedOperationException();
    }

    @PutMapping("/edit")
    public ResponseEntity<Shift> editShift(ShiftRequest shiftRequest,
                                           Authentication auth) {
        throw new UnsupportedOperationException();
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Shift> removeShift(Long shiftId,
                                             Authentication auth) {
        throw new UnsupportedOperationException();
    }
}
