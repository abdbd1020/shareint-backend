package com.shareint.backend.modules.location.controller;

import com.shareint.backend.modules.location.dto.LocationDTO;
import com.shareint.backend.modules.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/divisions")
    public ResponseEntity<List<LocationDTO>> getDivisions() {
        return ResponseEntity.ok(locationService.getDivisions());
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<LocationDTO>> getChildren(@PathVariable UUID parentId) {
        return ResponseEntity.ok(locationService.getChildren(parentId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<LocationDTO>> searchLocations(@RequestParam String q) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(locationService.searchLocations(q.trim()));
    }

    @GetMapping("/districts/search")
    public ResponseEntity<List<LocationDTO>> searchDistricts(@RequestParam String q) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(locationService.searchDistricts(q.trim()));
    }
}
