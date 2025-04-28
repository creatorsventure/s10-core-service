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
@RequiredArgsConstructor
@SuperBuilder
@MappedSuperclass
@FilterDef(name = ApplicationConstant.HIBERNATE_MERCHANT_FILTER_NAME, parameters = {
        @ParamDef(name = "unitId", type = String.class),
        @ParamDef(name = "merchantId", type = String.class)
})
@Filter(name = ApplicationConstant.HIBERNATE_MERCHANT_FILTER_NAME, condition = "unit_id = :unitId AND merchant_id = :merchantId")
public abstract class GenericMerchantEntity extends GenericEntity implements Serializable {


    @Serial
    private static final long serialVersionUID = -1724457128791641020L;

    @PrePersist
    @PreUpdate
    public void setValuesFromContext() {
        if (this.unitId == null) { // Only if not already set manually
            String ctxUnitId = RequestContext.get("unitId");
            if (ctxUnitId != null) {
                this.unitId = ctxUnitId;
            }
            String ctxMerchantId = RequestContext.get("merchantId");
            if (ctxMerchantId != null) {
                this.unitId = ctxMerchantId;
            }
        }
    }

    @Column(nullable = false)
    private String unitId;

    @Column(nullable = false)
    private String merchantId;

}
