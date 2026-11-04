package com.dormitory.exchange.repository;

import com.dormitory.exchange.entity.ExchangeRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ExchangeReputationRepository extends Repository<ExchangeRequest, Long> {

    /** Số giao dịch COMPLETED mà người này là người gửi yêu cầu hoặc là chủ tin. */
    @Query("SELECT COUNT(r) FROM ExchangeRequest r WHERE r.status = 'COMPLETED' AND "
         + "(r.requesterId = :userId OR r.itemId IN "
         + "(SELECT i.id FROM ExchangeItem i WHERE i.ownerId = :userId))")
    long countCompletedByUser(@Param("userId") Long userId);
}