package com.github.rodrigo_sp17.mscheduler.shift.data;

import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class Shift {

    /*
        AF:
            Period of time the friend is away at work and therefore unavailable
    *   Rep invariant:
    *       unavailabilityStartDate <= boardingDate <= leavingDate <= unavailabilityEndDate
    *       unavailabilityStartDate is NOT available
    *       unavailabilityEndDate is NOT available
    *   Safety from rep exposure:
    *       All return types are immutable
     */

    @Id
    private Long shiftId;

    // Identifies commonly created shifts for batch editing
    private Long shiftGroupId;

    @ManyToOne
    private AppUser owner;

    private LocalDate unavailabilityStartDate;

    private LocalDate boardingDate;

    private LocalDate leavingDate;

    private LocalDate unavailabilityEndDate;

    private void checkRep() {
        if (!((unavailabilityStartDate.isBefore(boardingDate)
                || unavailabilityStartDate.isEqual(boardingDate))
                && (boardingDate.isBefore(leavingDate) || boardingDate.isEqual(leavingDate))
                && (leavingDate.isBefore(unavailabilityEndDate)
                || leavingDate.isEqual(unavailabilityEndDate)))) {
            throw new AssertionError("Rep compromised on Shift class");
        }
    }

    public Shift() {
    }

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public Long getShiftGroupId() {
        return shiftGroupId;
    }

    public void setShiftGroupId(Long shiftGroupId) {
        this.shiftGroupId = shiftGroupId;
    }

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }

    public LocalDate getUnavailabilityStartDate() {
        return unavailabilityStartDate;
    }

    public void setUnavailabilityStartDate(LocalDate unavailabilityStartDate) {
        this.unavailabilityStartDate = unavailabilityStartDate;
        checkRep();
    }

    public LocalDate getBoardingDate() {
        return boardingDate;
    }

    public void setBoardingDate(LocalDate boardingDate) {
        this.boardingDate = boardingDate;
        checkRep();
    }

    public LocalDate getLeavingDate() {
        return leavingDate;
    }

    public void setLeavingDate(LocalDate leavingDate) {
        this.leavingDate = leavingDate;
        checkRep();
    }

    public LocalDate getUnavailabilityEndDate() {
        return unavailabilityEndDate;
    }

    public void setUnavailabilityEndDate(LocalDate unavailabilityEndDate) {
        this.unavailabilityEndDate = unavailabilityEndDate;
        checkRep();
    }
}
