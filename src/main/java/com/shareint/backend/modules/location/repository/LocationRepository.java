package com.shareint.backend.modules.location.repository;

import com.shareint.backend.modules.location.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
    
    List<Location> findByParentIsNullAndIsActiveTrueOrderByNameEnAsc();
    
    List<Location> findByParentIdAndIsActiveTrueOrderByNameEnAsc(UUID parentId);
    
    @Query("SELECT l FROM Location l WHERE l.isActive = true AND " +
           "(LOWER(l.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "l.nameBn LIKE CONCAT('%', :query, '%')) " +
           "ORDER BY l.nameEn ASC")
    List<Location> searchActiveLocations(@Param("query") String query);
}
