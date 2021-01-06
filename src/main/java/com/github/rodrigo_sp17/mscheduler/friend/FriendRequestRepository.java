package com.github.rodrigo_sp17.mscheduler.friend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    @Query("select fr from FriendRequest fr where fr.source.userInfo.username = :sourceName " +
            "and fr.target.userInfo.username = :targetName")
    FriendRequest findBySourceUsernameAndTargetUsername(String sourceName, String targetName);
}
