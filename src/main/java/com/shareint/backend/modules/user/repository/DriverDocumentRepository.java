package com.shareint.backend.modules.user.repository;

import com.shareint.backend.modules.user.model.DriverDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DriverDocumentRepository extends JpaRepository<DriverDocument, UUID> {
    List<DriverDocument> findAllByUserId(UUID userId);
    boolean existsByUserIdAndDocumentType(UUID userId, DriverDocument.DocumentType documentType);
}
