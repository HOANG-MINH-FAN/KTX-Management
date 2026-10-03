package com.dormitory.exchange.service.impl;

import com.dormitory.exchange.entity.ExchangeReview;
import com.dormitory.exchange.repository.ExchangeReviewRepository;
import com.dormitory.exchange.service.ExchangeReviewService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExchangeReviewServiceImpl implements ExchangeReviewService {

    private final ExchangeReviewRepository repository;

    public ExchangeReviewServiceImpl(ExchangeReviewRepository repository) {
        this.repository = repository;
    }

    public List<ExchangeReview> getAll() {
        return repository.findAll();
    }

    public List<ExchangeReview> getByRevieweeId(Long revieweeId) {
        return repository.findByRevieweeId(revieweeId);
    }

    public List<ExchangeReview> getByReviewerId(Long reviewerId) {
        return repository.findByReviewerId(reviewerId);
    }

    public Optional<ExchangeReview> getById(Long id) {
        return repository.findById(id);
    }

    public ExchangeReview save(ExchangeReview review) {
        return repository.save(review);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}