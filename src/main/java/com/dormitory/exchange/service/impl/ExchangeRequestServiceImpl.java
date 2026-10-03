package com.dormitory.exchange.service.impl;

import com.dormitory.exchange.entity.ExchangeRequest;
import com.dormitory.exchange.repository.ExchangeRequestRepository;
import com.dormitory.exchange.service.ExchangeRequestService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExchangeRequestServiceImpl implements ExchangeRequestService {

    private final ExchangeRequestRepository repository;

    public ExchangeRequestServiceImpl(ExchangeRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ExchangeRequest> getAll() {
        return repository.findAll();
    }

    @Override
    public List<ExchangeRequest> getByItemId(Long itemId) {
        return repository.findByItemId(itemId);
    }

    @Override
    public List<ExchangeRequest> getByRequesterId(Long requesterId) {
        return repository.findByRequesterId(requesterId);
    }

    @Override
    public Optional<ExchangeRequest> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public ExchangeRequest save(ExchangeRequest request) {
        return repository.save(request);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}