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

    /** Người đánh giá tạo đánh giá cho giao dịch đã hoàn thành. Vi phạm luật thì ném IllegalArgumentException. */
    ExchangeReview createReview(Long reviewerId, Long requestId, int rating, String comment);

    /** Điểm trung bình người này nhận được; null nếu chưa có đánh giá. */
    Double getAverageRating(Long userId);
}