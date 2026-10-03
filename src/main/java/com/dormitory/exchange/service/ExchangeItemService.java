package com.dormitory.exchange.service;

import com.dormitory.exchange.entity.ExchangeItem;

import java.util.List;
import java.util.Optional;

public interface ExchangeItemService {

    List<ExchangeItem> getAll();

    List<ExchangeItem> getAvailable();

    Optional<ExchangeItem> getById(Long id);

    ExchangeItem save(ExchangeItem item);

    void delete(Long id);
}