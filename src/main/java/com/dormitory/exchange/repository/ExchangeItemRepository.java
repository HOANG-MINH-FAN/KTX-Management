package com.dormitory.exchange.repository;

import com.dormitory.exchange.entity.ExchangeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangeItemRepository extends JpaRepository<ExchangeItem, Long> {

    List<ExchangeItem> findByStatus(String status);

    List<ExchangeItem> findByOwnerId(Long ownerId);

    List<ExchangeItem> findByCategoryAndStatus(String category, String status);
}