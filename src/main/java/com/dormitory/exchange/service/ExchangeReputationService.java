package com.dormitory.exchange.service;

import com.dormitory.exchange.repository.ExchangeReputationRepository;
import org.springframework.stereotype.Service;

@Service
public class ExchangeReputationService {

    private final ExchangeReviewService reviewService;
    private final ExchangeReputationRepository reputationRepository;

    public ExchangeReputationService(ExchangeReviewService reviewService,
                                     ExchangeReputationRepository reputationRepository) {
        this.reviewService = reviewService;
        this.reputationRepository = reputationRepository;
    }

    public Reputation getReputation(Long userId) {
        Double average = reviewService.getAverageRating(userId);
        int reviewCount = reviewService.getByRevieweeId(userId).size();
        long completed = reputationRepository.countCompletedByUser(userId);
        return new Reputation(average, reviewCount, completed);
    }

    /** Gói 3 số liệu uy tín để đưa ra giao diện. */
    public static class Reputation {
        private final Double averageRating;   // null nếu chưa có đánh giá
        private final int reviewCount;
        private final long completedCount;

        public Reputation(Double averageRating, int reviewCount, long completedCount) {
            this.averageRating = averageRating;
            this.reviewCount = reviewCount;
            this.completedCount = completedCount;
        }

        public Double getAverageRating() { return averageRating; }
        public int getReviewCount() { return reviewCount; }
        public long getCompletedCount() { return completedCount; }

        /** Chuỗi sao để hiển thị, ví dụ 4.5 -> ★★★★★ (làm tròn), 3.2 -> ★★★☆☆ */
        public String getStarsText() {
            if (averageRating == null) return "";
            int full = Math.max(0, Math.min(5, (int) Math.round(averageRating)));
            return "★".repeat(full) + "☆".repeat(5 - full);
        }
    }
}