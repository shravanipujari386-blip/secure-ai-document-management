
package com.lawenforcement.repository;

import com.lawenforcement.entity.CaseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CaseRepository extends JpaRepository<CaseRecord, Long> {


Optional<CaseRecord> findByCaseNumber(String caseNumber);

@Query("SELECT DISTINCT c FROM CaseRecord c LEFT JOIN FETCH c.assignedOfficer")
List<CaseRecord> findAllWithAssignedOfficer();

@Query("SELECT c FROM CaseRecord c LEFT JOIN FETCH c.assignedOfficer WHERE c.id = :id")
Optional<CaseRecord> findByIdWithAssignedOfficer(@Param("id") Long id);
@Query("SELECT DISTINCT c FROM CaseRecord c " +
	       "LEFT JOIN FETCH c.assignedOfficer " +
	       "WHERE c.assignedOfficer.id = :officerId")
	List<CaseRecord> findCasesAssignedToOfficer(
	        @Param("officerId") Long officerId);

}

