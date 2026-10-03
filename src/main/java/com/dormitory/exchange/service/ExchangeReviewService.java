package com.dormitory.exchange.service;

import com.dormitory.exchange.entity.ExchangeReview;

import java.util.List;
import java.util.Optional;

public interface ExchangeReviewService {

    List<ExchangeReview> getAll();

    List<ExchangeReview> getByRevieweeId(Long revieweeId);

    List<ExchangeReview> getByReviewerId(Long reviewerId);

    Optional<ExchangeReview> getById(Long id);

    ExchangeReview save(ExchangeReview review);

    void delete(Long id);
}