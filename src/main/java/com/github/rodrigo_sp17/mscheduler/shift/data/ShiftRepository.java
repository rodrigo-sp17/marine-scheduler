package com.github.rodrigo_sp17.mscheduler.shift.data;

import com.github.rodrigo_sp17.mscheduler.shift.data.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
}
