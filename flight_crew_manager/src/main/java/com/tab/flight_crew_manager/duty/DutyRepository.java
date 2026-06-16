package com.tab.flight_crew_manager.duty;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DutyRepository extends JpaRepository<Duty, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Duty d where d.id = :id")
    Optional<Duty> findByIdForUpdate(@Param("id") Long id);
}
