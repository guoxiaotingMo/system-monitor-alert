package org.apache.hertzbeat.common.entity.manager;

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
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.hertzbeat.common.entity.alerter.JsonMapAttributeConverter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "hzb_project", indexes = {
    @Index(name = "idx_hzb_project_name", columnList = "name")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Project Entity for multi-project management")
@EntityListeners(AuditingEntityListener.class)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(title = "Project ID", accessMode = READ_ONLY)
    private Long id;

    @Schema(title = "Project name", accessMode = READ_WRITE)
    @Size(max = 100)
    private String name;

    @Schema(title = "Project description", accessMode = READ_WRITE)
    @Size(max = 255)
    private String description;

    @Schema(title = "Status: 0=disabled, 1=enabled", accessMode = READ_WRITE)
    private byte status;

    @Schema(title = "Custom labels", accessMode = READ_WRITE)
    @Convert(converter = JsonMapAttributeConverter.class)
    @Column(length = 4096)
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
