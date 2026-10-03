package com.dormitory.exchange.service.impl;

import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.repository.ExchangeItemRepository;
import com.dormitory.exchange.service.ExchangeItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExchangeItemServiceImpl implements ExchangeItemService {

    private final ExchangeItemRepository repository;

    public ExchangeItemServiceImpl(ExchangeItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ExchangeItem> getAll() {
        return repository.findAll();
    }

    @Override
    public List<ExchangeItem> getAvailable() {
        return repository.findByStatus("AVAILABLE");
    }

    @Override
    public Optional<ExchangeItem> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public ExchangeItem save(ExchangeItem item) {
        return repository.save(item);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}