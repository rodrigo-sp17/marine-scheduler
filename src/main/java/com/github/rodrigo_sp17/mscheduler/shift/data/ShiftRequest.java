package com.github.rodrigo_sp17.mscheduler.shift.data;

import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;

import java.time.LocalDate;

public class ShiftRequest {

    private Long shiftId;

    private LocalDate unavailabilityStartDate;

    private LocalDate boardingDate;

    private LocalDate leavingDate;

    private LocalDate unavailabilityEndDate;

    // Regular number of shift days to use for calculations
    // i.e: 14, 28, 35, 56. 0 == null.
    private Long cycleDays;

    // Times to repeat the schedule
    private Long repeat;


    public ShiftRequest() {
    }

    // Getters and Setters
    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public LocalDate getUnavailabilityStartDate() {
        return unavailabilityStartDate;
    }

    public void setUnavailabilityStartDate(LocalDate unavailabilityStartDate) {
        this.unavailabilityStartDate = unavailabilityStartDate;
    }

    public LocalDate getBoardingDate() {
        return boardingDate;
    }

    public void setBoardingDate(LocalDate boardingDate) {
        this.boardingDate = boardingDate;
    }

    public LocalDate getLeavingDate() {
        return leavingDate;
    }

    public void setLeavingDate(LocalDate leavingDate) {
        this.leavingDate = leavingDate;
    }

    public LocalDate getUnavailabilityEndDate() {
        return unavailabilityEndDate;
    }

    public void setUnavailabilityEndDate(LocalDate unavailabilityEndDate) {
        this.unavailabilityEndDate = unavailabilityEndDate;
    }

    public Long getCycleDays() {
        return cycleDays;
    }

    public void setCycleDays(Long cycleDays) {
        this.cycleDays = cycleDays;
    }

    public Long getRepeat() {
        return repeat;
    }

    public void setRepeat(Long repeat) {
        this.repeat = repeat;
    }
}
