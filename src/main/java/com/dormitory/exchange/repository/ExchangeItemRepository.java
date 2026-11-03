package com.dormitory.exchange.repository;

import com.dormitory.exchange.entity.ExchangeItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExchangeItemRepository extends JpaRepository<ExchangeItem, Long> {

    List<ExchangeItem> findByStatus(String status);

    List<ExchangeItem> findByOwnerId(Long ownerId);

    List<ExchangeItem> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<ExchangeItem> findByCategoryAndStatus(String category, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM ExchangeItem i WHERE i.id = :id")
    Optional<ExchangeItem> findByIdForUpdate(@Param("id") Long id);
}
