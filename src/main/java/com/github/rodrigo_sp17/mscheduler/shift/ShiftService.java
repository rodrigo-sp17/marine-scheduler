package com.github.rodrigo_sp17.mscheduler.shift;

import com.github.rodrigo_sp17.mscheduler.shift.data.Shift;
import com.github.rodrigo_sp17.mscheduler.shift.data.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShiftService {

    @Autowired
    private final ShiftRepository shiftRepository;

    public ShiftService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public List<Shift> getShifts() {
        throw new UnsupportedOperationException();
    }

    @Transactional
    public Shift saveShift(Shift shift) {
        throw new UnsupportedOperationException();
    }

    @Transactional
    public Shift removeShift(Long shiftId) {
        throw new UnsupportedOperationException();
    }
}
