package com.dormitory.exchange.repository;

import com.dormitory.exchange.entity.ExchangeRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExchangeRequestRepository
        extends JpaRepository<ExchangeRequest, Long> {

    List<ExchangeRequest> findByItemId(Long itemId);

    List<ExchangeRequest> findByItemIdOrderByCreatedAtDesc(Long itemId);

    List<ExchangeRequest> findByRequesterId(Long requesterId);

    List<ExchangeRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    List<ExchangeRequest> findByRequesterIdAndStatusOrderByCreatedAtDesc(Long requesterId, String status);

    List<ExchangeRequest> findByStatus(String status);

    Optional<ExchangeRequest> findByItemIdAndRequesterId(Long itemId, Long requesterId);

    boolean existsByItemIdAndRequesterIdAndStatus(Long itemId, Long requesterId, String status);

    boolean existsByItemIdAndStatusIn(Long itemId, Collection<String> statuses);

    List<ExchangeRequest> findByItemIdInOrderByCreatedAtDesc(Collection<Long> itemIds);

    List<ExchangeRequest> findByItemIdInAndStatusOrderByCreatedAtDesc(Collection<Long> itemIds, String status);

    @Query("SELECT r FROM ExchangeRequest r WHERE r.itemId IN " +
           "(SELECT i.id FROM ExchangeItem i WHERE i.ownerId = :ownerId) " +
           "ORDER BY r.createdAt DESC")
    List<ExchangeRequest> findReceivedRequestsByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT r FROM ExchangeRequest r WHERE " +
           "(r.requesterId = :studentId OR r.itemId IN (SELECT i.id FROM ExchangeItem i WHERE i.ownerId = :studentId)) " +
           "AND r.status IN :statuses " +
           "ORDER BY r.updatedAt DESC, r.createdAt DESC")
    List<ExchangeRequest> findHistoryByStudentIdAndStatuses(@Param("studentId") Long studentId,
                                                            @Param("statuses") Collection<String> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ExchangeRequest r WHERE r.id = :id")
    Optional<ExchangeRequest> findByIdForUpdate(@Param("id") Long id);
}
