package com.shareint.backend.modules.user.service;

import com.shareint.backend.core.exception.ResourceNotFoundException;
import com.shareint.backend.modules.trip.model.Trip;
import com.shareint.backend.modules.trip.repository.TripRepository;
import com.shareint.backend.modules.user.dto.CreateRatingRequest;
import com.shareint.backend.modules.user.dto.RatingDTO;
import com.shareint.backend.modules.user.model.Rating;
import com.shareint.backend.modules.user.model.User;
import com.shareint.backend.modules.user.repository.RatingRepository;
import com.shareint.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    @Transactional
    public RatingDTO submitRating(String reviewerPhoneNumber, CreateRatingRequest request) {
        User reviewer = userRepository.findByPhoneNumber(reviewerPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        User reviewee = userRepository.findById(request.getRevieweeId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewee not found"));

        if (reviewer.getId().equals(reviewee.getId())) {
            throw new IllegalArgumentException("You cannot rate yourself");
        }

        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        // In a real app, verify they actually traveled together on this trip
        // e.g. one is driver, one is passenger with a CONFIRMED/COMPLETED booking

        if (ratingRepository.existsByTripIdAndReviewerIdAndRevieweeId(trip.getId(), reviewer.getId(), reviewee.getId())) {
            throw new IllegalArgumentException("You have already rated this user for this trip");
        }

        Rating rating = Rating.builder()
                .trip(trip)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .score(request.getScore())
                .comment(request.getComment())
                .creator(reviewer)
                .updater(reviewer)
                .build();

        rating = ratingRepository.save(rating);

        updateUserAverageRating(reviewee);

        return mapToDTO(rating);
    }

    @Transactional(readOnly = true)
    public List<RatingDTO> getUserRatings(UUID userId) {
        return ratingRepository.findByRevieweeIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private void updateUserAverageRating(User user) {
        Double avgRating = ratingRepository.calculateAverageRatingByRevieweeId(user.getId()).orElse(0.0);
        BigDecimal roundedAvg = BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP);
        user.setAverageRating(roundedAvg);
        userRepository.save(user);
    }

    private RatingDTO mapToDTO(Rating rating) {
        return RatingDTO.builder()
                .id(rating.getId())
                .tripId(rating.getTrip().getId())
                .reviewerId(rating.getReviewer().getId())
                .reviewerName(rating.getReviewer().getFullName())
                .revieweeId(rating.getReviewee().getId())
                .score(rating.getScore())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}
