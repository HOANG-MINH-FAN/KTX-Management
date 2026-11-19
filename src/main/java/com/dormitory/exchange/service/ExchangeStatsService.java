package com.dormitory.exchange.service;

import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.repository.ExchangeItemRepository;
import com.dormitory.exchange.repository.ExchangeRequestRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

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

    /** Số tin đăng theo từng danh mục, ví dụ {"Sách"=9, "Điện tử"=15}. */
    public Map<String, Long> itemsByCategory() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (ExchangeItem item : itemRepository.findAll()) {
            String key = (item.getCategory() == null) ? "Khác" : item.getCategory();
            result.put(key, result.getOrDefault(key, 0L) + 1);
        }
        return result;
    }
}