package com.shareint.backend.modules.location.service;

import com.shareint.backend.modules.location.dto.LocationDTO;
import com.shareint.backend.modules.location.model.Location;
import com.shareint.backend.modules.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    @Transactional(readOnly = true)
    public List<LocationDTO> getDivisions() {
        return locationRepository.findByParentIsNullAndIsActiveTrueOrderByNameEnAsc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationDTO> getChildren(UUID parentId) {
        return locationRepository.findByParentIdAndIsActiveTrueOrderByNameEnAsc(parentId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationDTO> searchLocations(String query) {
        return locationRepository.searchActiveLocations(query)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationDTO> searchDistricts(String query) {
        return locationRepository.searchDistricts(query)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationDTO> searchZillasAndUpazillas(String query) {
        return locationRepository.searchZillasAndUpazillas(query)
                .stream()
                .map(this::mapToDTOWithParent)
                .collect(Collectors.toList());
    }

    private LocationDTO mapToDTO(Location location) {
        return LocationDTO.builder()
                .id(location.getId())
                .nameEn(location.getNameEn())
                .nameBn(location.getNameBn())
                .parentId(location.getParent() != null ? location.getParent().getId() : null)
                .isDistanceConsidered(location.isDistanceConsidered())
                .isActive(location.isActive())
                .build();
    }

    private LocationDTO mapToDTOWithParent(Location location) {
        UUID parentId = null;
        String parentNameEn = null;
        if (location.getParent() != null) {
            parentId = location.getParent().getId();
            parentNameEn = location.getParent().getNameEn();
        }
        return LocationDTO.builder()
                .id(location.getId())
                .nameEn(location.getNameEn())
                .nameBn(location.getNameBn())
                .parentId(parentId)
                .parentNameEn(parentNameEn)
                .isDistanceConsidered(location.isDistanceConsidered())
                .isActive(location.isActive())
                .build();
    }
}
