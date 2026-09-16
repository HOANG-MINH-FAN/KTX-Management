package com.dormitory.entity.enums;

/**
 * Trạng thái hợp đồng ở.
 */
public enum ContractStatus {
    /** Hợp đồng đang hiệu lực */
    ACTIVE,
    /** Hợp đồng đã hết hạn tự nhiên (end_date đã qua) */
    EXPIRED,
    /** Hợp đồng bị chấm dứt sớm (do vi phạm hoặc tự nguyện rời đi) */
    TERMINATED
}
