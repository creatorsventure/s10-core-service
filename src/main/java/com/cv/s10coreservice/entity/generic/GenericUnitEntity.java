package com.cv.s10coreservice.entity.generic;

import com.cv.s10coreservice.constant.ApplicationConstant;
import com.cv.s10coreservice.context.RequestContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
@MappedSuperclass
@FilterDef(name = ApplicationConstant.HIBERNATE_UNIT_FILTER_NAME, parameters = {
        @ParamDef(name = ApplicationConstant.HIBERNATE_PARAM_UNIT_ID, type = String.class)
})
@Filter(name = ApplicationConstant.HIBERNATE_UNIT_FILTER_NAME, condition = "unit_id = :unitId")
public abstract class GenericUnitEntity extends GenericEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 8945882357182749646L;

    @PrePersist
    @PreUpdate
    public void setValuesFromContext() {
        if (this.unitId == null) { // Only if not already set manually
            String ctxUnitId = RequestContext.get("unitId");
            if (ctxUnitId != null) {
                this.unitId = ctxUnitId;
            }
        }
    }

    @Column(nullable = false)
    private String unitId;

}
