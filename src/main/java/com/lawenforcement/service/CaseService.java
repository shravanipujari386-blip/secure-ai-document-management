package com.lawenforcement.service;

import com.lawenforcement.entity.CaseRecord;
import com.lawenforcement.entity.User;
import com.lawenforcement.repository.CaseRepository;
import com.lawenforcement.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CaseService {


private final CaseRepository caseRepository;
private final UserRepository userRepository;

public CaseService(
        CaseRepository caseRepository,
        UserRepository userRepository) {

    this.caseRepository = caseRepository;
    this.userRepository = userRepository;
}

@Transactional
public CaseRecord createAndAssignCase(
        CaseRecord caseRecord,
        Long officerId,
        User admin) {

    User officer = userRepository.findById(officerId)
            .orElseThrow(() ->
                    new IllegalArgumentException("Officer not found."));

    if (!"INVESTIGATOR OFFICER".equalsIgnoreCase(officer.getRole())
            && !"OFFICER".equalsIgnoreCase(officer.getRole())) {

        throw new IllegalArgumentException(
                "The selected user is not an investigator officer.");
    }

    if (!"ACTIVE".equalsIgnoreCase(officer.getAccountStatus())) {
        throw new IllegalArgumentException(
                "Only active officers can receive cases.");
    }

    if (caseRecord.getCaseNumber() == null
            || caseRecord.getCaseNumber().isBlank()
            || caseRecord.getTitle() == null
            || caseRecord.getTitle().isBlank()) {

        throw new IllegalArgumentException(
                "Case number and title are required.");
    }

    if (caseRepository.findByCaseNumber(
            caseRecord.getCaseNumber().trim()).isPresent()) {

        throw new IllegalArgumentException(
                "A case with this case number already exists.");
    }

    caseRecord.setCaseNumber(caseRecord.getCaseNumber().trim());
    caseRecord.setTitle(caseRecord.getTitle().trim());

    caseRecord.setStatus(
            caseRecord.getStatus() == null
                    || caseRecord.getStatus().isBlank()
                    ? "OPEN"
                    : caseRecord.getStatus());

    caseRecord.setAssignedOfficer(officer);
    caseRecord.setAssignedBy(admin);
    caseRecord.setAssignedAt(LocalDateTime.now());

    return caseRepository.save(caseRecord);
}

@Transactional(readOnly = true)
public List<CaseRecord> getCasesAssignedToOfficer(Long officerId) {
    return caseRepository.findCasesAssignedToOfficer(officerId);
}

@Transactional(readOnly = true)
public List<CaseRecord> getAllCases() {
    return caseRepository.findAllWithAssignedOfficer();
}


}
