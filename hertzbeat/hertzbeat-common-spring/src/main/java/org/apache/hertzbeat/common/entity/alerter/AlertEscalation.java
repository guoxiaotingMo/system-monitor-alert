package org.apache.hertzbeat.common.entity.alerter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.hertzbeat.common.entity.manager.JsonLongListAttributeConverter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "hzb_alert_escalation", indexes = {
    @Index(name = "idx_hzb_ae_priority", columnList = "fromPriority"),
    @Index(name = "idx_hzb_ae_project", columnList = "projectId")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Alert Escalation Rule Entity")
@EntityListeners(AuditingEntityListener.class)
public class AlertEscalation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(title = "Escalation Rule ID", accessMode = READ_ONLY)
    private Long id;

    @Schema(title = "Rule name", example = "critical-escalation", accessMode = READ_WRITE)
    @Size(max = 100) @NotBlank
    private String name;

    @Schema(title = "Project ID", accessMode = READ_WRITE)
    private Long projectId;

    @Schema(title = "From priority level (1=Emergency, 2=Critical, 3=Warning, 4=Info)", accessMode = READ_WRITE)
    @Min(1) @Max(4) @NotNull
    private byte fromPriority;

    @Schema(title = "To priority level after escalation", accessMode = READ_WRITE)
    @Min(1) @Max(4) @NotNull
    private byte toPriority;

    @Schema(title = "Escalation timeout in seconds", example = "900", accessMode = READ_WRITE)
    @Min(60) @NotNull
    private Integer timeoutSeconds;

    @Schema(title = "Escalation receiver IDs (additional receivers after escalation)", accessMode = READ_WRITE)
    @Convert(converter = JsonLongListAttributeConverter.class)
    private List<Long> escalationReceiverIds;

    @Schema(title = "Whether enabled", example = "true", accessMode = READ_WRITE)
    private boolean enable;

    @Schema(title = "Labels to match for this rule", accessMode = READ_WRITE)
    @Convert(converter = JsonMapAttributeConverter.class)
    @Column(length = 2048)
    private Map<String, String> labels;

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
