package org.apache.hertzbeat.common.entity.manager;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "hzb_service_availability", indexes = {
    @Index(name = "idx_hzb_sa_monitor", columnList = "monitorId"),
    @Index(name = "idx_hzb_sa_project", columnList = "projectId")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Service Availability Entity for SLA tracking")
@EntityListeners(AuditingEntityListener.class)
public class ServiceAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(title = "Service Availability ID", accessMode = READ_ONLY)
    private Long id;

    @Schema(title = "Monitor ID", accessMode = READ_WRITE)
    @NotNull
    private Long monitorId;

    @Schema(title = "Project ID", accessMode = READ_WRITE)
    private Long projectId;

    @Schema(title = "Consecutive failure count", example = "0", accessMode = READ_WRITE)
    @Min(0)
    private Integer consecutiveFailures;

    @Schema(title = "Failure threshold for marking unavailable", example = "3", accessMode = READ_WRITE)
    @Min(1) @Max(100)
    private Integer failureThreshold;

    @Schema(title = "Availability status: 0-available, 1-degraded, 2-unavailable", accessMode = READ_WRITE)
    @Min(0) @Max(2)
    private byte availabilityStatus;

    @Schema(title = "Total check count in SLA period", accessMode = READ_WRITE)
    private Long totalChecks;

    @Schema(title = "Successful check count in SLA period", accessMode = READ_WRITE)
    private Long successfulChecks;

    @Schema(title = "SLA availability percentage (0.00-100.00)", example = "99.95", accessMode = READ_WRITE)
    private Double slaPercentage;

    @Schema(title = "SLA period start time", accessMode = READ_WRITE)
    private LocalDateTime slaPeriodStart;

    @Schema(title = "Last check time", accessMode = READ_ONLY)
    private LocalDateTime lastCheckTime;

    @Schema(title = "Last status change time", accessMode = READ_ONLY)
    private LocalDateTime lastStatusChangeTime;

    @Schema(title = "Creator", accessMode = READ_ONLY)
    @CreatedBy
    private String creator;

    @Schema(title = "Modifier", accessMode = READ_ONLY)
    @LastModifiedBy
    private String modifier;

    @Schema(title = "Create time", accessMode = READ_ONLY)
    @CreatedDate
    private LocalDateTime gmtCreate;

    @Schema(title = "Update time", accessMode = READ_ONLY)
    @LastModifiedDate
    private LocalDateTime gmtUpdate;
}
