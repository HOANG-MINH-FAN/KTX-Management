package com.dormitory.exchange.service;

import com.dormitory.exchange.entity.ExchangeRequest;

import java.util.List;
import java.util.Optional;

public interface ExchangeRequestService {

    List<ExchangeRequest> getAll();

    List<ExchangeRequest> getByItemId(Long itemId);

    List<ExchangeRequest> getByRequesterId(Long requesterId);

    Optional<ExchangeRequest> getById(Long id);

    ExchangeRequest save(ExchangeRequest request);

    void delete(Long id);
}