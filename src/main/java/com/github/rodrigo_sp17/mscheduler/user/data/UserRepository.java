package com.github.rodrigo_sp17.mscheduler.user.data;

import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
}
