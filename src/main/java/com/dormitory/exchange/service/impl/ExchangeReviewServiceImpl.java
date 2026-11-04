package com.dormitory.exchange.service.impl;

import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.entity.ExchangeRequest;
import com.dormitory.exchange.entity.ExchangeReview;
import com.dormitory.exchange.repository.ExchangeItemRepository;
import com.dormitory.exchange.repository.ExchangeRequestRepository;
import com.dormitory.exchange.repository.ExchangeReviewRepository;
import com.dormitory.exchange.service.ExchangeReviewService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExchangeReviewServiceImpl implements ExchangeReviewService {

    private final ExchangeReviewRepository repository;
    private final ExchangeRequestRepository requestRepository;
    private final ExchangeItemRepository itemRepository;

    public ExchangeReviewServiceImpl(ExchangeReviewRepository repository,
                                     ExchangeRequestRepository requestRepository,
                                     ExchangeItemRepository itemRepository) {
        this.repository = repository;
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
    }

    @Override public List<ExchangeReview> getAll() { return repository.findAll(); }
    @Override public List<ExchangeReview> getByRevieweeId(Long id) { return repository.findByRevieweeId(id); }
    @Override public List<ExchangeReview> getByReviewerId(Long id) { return repository.findByReviewerId(id); }
    @Override public Optional<ExchangeReview> getById(Long id) { return repository.findById(id); }
    @Override public ExchangeReview save(ExchangeReview review) { return repository.save(review); }
    @Override public void delete(Long id) { repository.deleteById(id); }

    @Override
    public ExchangeReview createReview(Long reviewerId, Long requestId, int rating, String comment) {
        ExchangeRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch."));
        if (!"COMPLETED".equals(request.getStatus())) {
            throw new IllegalArgumentException("Chỉ được đánh giá giao dịch đã hoàn thành.");
        }
        ExchangeItem item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Tin đăng của giao dịch không còn tồn tại."));

        Long ownerId = item.getOwnerId();
        Long requesterId = request.getRequesterId();
        Long revieweeId;
        if (reviewerId.equals(requesterId)) {
            revieweeId = ownerId;
        } else if (reviewerId.equals(ownerId)) {
            revieweeId = requesterId;
        } else {
            throw new IllegalArgumentException("Bạn không tham gia giao dịch này.");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5.");
        }
        if (repository.existsByRequestIdAndReviewerId(requestId, reviewerId)) {
            throw new IllegalArgumentException("Bạn đã đánh giá giao dịch này rồi.");
        }

        ExchangeReview review = new ExchangeReview();
        review.setRequestId(requestId);
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setRating(rating);
        review.setComment(comment == null ? null : comment.trim());
        return repository.save(review);
    }

    @Override
    public Double getAverageRating(Long userId) {
        return repository.findAverageRating(userId);
    }
}