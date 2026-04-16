package com.tab.flight_crew_manager.crew_assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrewAssignmentRepository extends JpaRepository<CrewAssignment, Long> {
}
