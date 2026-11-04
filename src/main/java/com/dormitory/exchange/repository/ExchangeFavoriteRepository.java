package com.dormitory.exchange.repository;

import com.dormitory.exchange.entity.ExchangeFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExchangeFavoriteRepository extends JpaRepository<ExchangeFavorite, Long> {

    List<ExchangeFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<ExchangeFavorite> findByUserIdAndItemId(Long userId, Long itemId);
}