
package com.lawenforcement.repository;

import com.lawenforcement.entity.DocumentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository
        extends JpaRepository<DocumentRecord, Long> {

    List<DocumentRecord> findAllByOrderByUploadedAtDesc();
}



