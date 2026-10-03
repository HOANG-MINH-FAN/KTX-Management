package com.dormitory.exchange.repository;

import com.dormitory.exchange.entity.ExchangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangeRequestRepository
        extends JpaRepository<ExchangeRequest, Long> {

    List<ExchangeRequest> findByItemId(Long itemId);

    List<ExchangeRequest> findByRequesterId(Long requesterId);

    List<ExchangeRequest> findByStatus(String status);
}