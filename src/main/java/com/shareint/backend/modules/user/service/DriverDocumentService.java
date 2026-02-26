package com.shareint.backend.modules.user.service;

import com.shareint.backend.core.exception.ResourceNotFoundException;
import com.shareint.backend.modules.auth.dto.SubmitDriverDocumentsRequest;
import com.shareint.backend.modules.user.dto.DriverDocumentDTO;
import com.shareint.backend.modules.user.model.DriverDocument;
import com.shareint.backend.modules.user.model.User;
import com.shareint.backend.modules.user.repository.DriverDocumentRepository;
import com.shareint.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverDocumentService {

    private final DriverDocumentRepository driverDocumentRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<DriverDocumentDTO> submitDocuments(UUID userId, SubmitDriverDocumentsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isProfileComplete()) {
            throw new IllegalStateException("Profile must be complete before submitting driver documents");
        }

        // Upsert NID document
        DriverDocument nidDoc = driverDocumentRepository
                .findAllByUserId(userId).stream()
                .filter(doc -> doc.getDocumentType() == DriverDocument.DocumentType.NID)
                .findFirst()
                .orElse(new DriverDocument());

        nidDoc.setUser(user);
        nidDoc.setDocumentType(DriverDocument.DocumentType.NID);
        nidDoc.setDocumentUrl(request.getNidDocumentUrl());
        nidDoc.setStatus(DriverDocument.DocumentStatus.PENDING);
        nidDoc.setUpdatedAt(Instant.now());
        driverDocumentRepository.save(nidDoc);

        // Upsert Driver's License document
        DriverDocument licenseDoc = driverDocumentRepository
                .findAllByUserId(userId).stream()
                .filter(doc -> doc.getDocumentType() == DriverDocument.DocumentType.DRIVERS_LICENSE)
                .findFirst()
                .orElse(new DriverDocument());

        licenseDoc.setUser(user);
        licenseDoc.setDocumentType(DriverDocument.DocumentType.DRIVERS_LICENSE);
        licenseDoc.setDocumentUrl(request.getDriversLicenseUrl());
        licenseDoc.setStatus(DriverDocument.DocumentStatus.PENDING);
        licenseDoc.setUpdatedAt(Instant.now());
        driverDocumentRepository.save(licenseDoc);

        // Upsert Fitness Certificate document
        DriverDocument fitnessDoc = driverDocumentRepository
                .findAllByUserId(userId).stream()
                .filter(doc -> doc.getDocumentType() == DriverDocument.DocumentType.FITNESS_CERTIFICATE)
                .findFirst()
                .orElse(new DriverDocument());

        fitnessDoc.setUser(user);
        fitnessDoc.setDocumentType(DriverDocument.DocumentType.FITNESS_CERTIFICATE);
        fitnessDoc.setDocumentUrl(request.getFitnessCertificateUrl());
        fitnessDoc.setStatus(DriverDocument.DocumentStatus.PENDING);
        fitnessDoc.setUpdatedAt(Instant.now());
        driverDocumentRepository.save(fitnessDoc);

        return getStatus(userId);
    }

    @Transactional(readOnly = true)
    public List<DriverDocumentDTO> getStatus(UUID userId) {
        return driverDocumentRepository.findAllByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private DriverDocumentDTO mapToDTO(DriverDocument doc) {
        return DriverDocumentDTO.builder()
                .id(doc.getId())
                .documentType(doc.getDocumentType().name())
                .status(doc.getStatus().name())
                .documentUrl(doc.getDocumentUrl())
                .reviewedAt(doc.getReviewedAt())
                .rejectionNote(doc.getRejectionNote())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}
