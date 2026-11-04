package com.dormitory.exchange.service;

import com.dormitory.exchange.repository.ExchangeItemRepository;
import com.dormitory.exchange.repository.ExchangeRequestRepository;
import org.springframework.stereotype.Service;

@Service
public class ExchangeStatsService {

    private final ExchangeItemRepository itemRepository;
    private final ExchangeRequestRepository requestRepository;

    public ExchangeStatsService(ExchangeItemRepository itemRepository,
                                ExchangeRequestRepository requestRepository) {
        this.itemRepository = itemRepository;
        this.requestRepository = requestRepository;
    }

    /** Tổng số tin đã đăng (mọi trạng thái). */
    public long totalItems() { return itemRepository.count(); }

    /** Tin đang hoạt động = status AVAILABLE. */
    public long activeItems() { return itemRepository.findByStatus("AVAILABLE").size(); }

    /** Giao dịch hoàn thành = yêu cầu có status COMPLETED. */
    public long completedTransactions() { return requestRepository.findByStatus("COMPLETED").size(); }

    /** Đồ được tái sử dụng = số tin đã chốt thành công (SOLD hoặc EXCHANGED). */
    public long reusedItems() {
        return itemRepository.findByStatus("SOLD").size()
             + itemRepository.findByStatus("EXCHANGED").size();
    }
}