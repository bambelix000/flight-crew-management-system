package com.tab.flight_crew_manager.duty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DutyRepository extends JpaRepository<Duty, Long> {

}
