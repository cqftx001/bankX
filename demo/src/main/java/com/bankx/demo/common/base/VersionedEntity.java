package com.bankx.demo.common.base;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

@MappedSuperclass
public class VersionedEntity extends BaseEntity {

    // 乐观锁 - 银行系统
    @Version
    @Column(nullable = false)
    @Schema(description = "Optimistic lock version — managed by JPA, never set manually")
    private Long version;

}
