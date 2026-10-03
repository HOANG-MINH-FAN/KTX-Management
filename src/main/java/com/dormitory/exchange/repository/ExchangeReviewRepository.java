package com.dormitory.exchange.repository;

import com.dormitory.exchange.entity.ExchangeReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangeReviewRepository
        extends JpaRepository<ExchangeReview, Long> {

    List<ExchangeReview> findByRevieweeId(Long revieweeId);

    List<ExchangeReview> findByReviewerId(Long reviewerId);
}