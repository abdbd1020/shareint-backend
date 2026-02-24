package com.shareint.backend.modules.user.repository;

import com.shareint.backend.modules.user.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {
    
    List<Rating> findByRevieweeIdOrderByCreatedAtDesc(UUID revieweeId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.reviewee.id = :revieweeId")
    Optional<Double> calculateAverageRatingByRevieweeId(UUID revieweeId);

    boolean existsByTripIdAndReviewerIdAndRevieweeId(UUID tripId, UUID reviewerId, UUID revieweeId);
}
