package com.dormitory.exchange.repository;

import com.dormitory.exchange.entity.ExchangeReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExchangeReviewRepository extends JpaRepository<ExchangeReview, Long> {

    List<ExchangeReview> findByRevieweeId(Long revieweeId);

    List<ExchangeReview> findByReviewerId(Long reviewerId);

    boolean existsByRequestIdAndReviewerId(Long requestId, Long reviewerId);

    @Query("select avg(r.rating) from ExchangeReview r where r.revieweeId = :userId")
    Double findAverageRating(@Param("userId") Long userId);
}