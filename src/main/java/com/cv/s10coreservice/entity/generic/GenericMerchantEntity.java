package com.cv.s10coreservice.entity.generic;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@FilterDef(name = "contextFilter", parameters = {
        @ParamDef(name = "unitId", type = String.class),
        @ParamDef(name = "merchantId", type = String.class)
})
@Filter(name = "contextFilter", condition = "unit_id = :unitId AND merchant_id = :merchantId")
public abstract class GenericMerchantEntity extends GenericUnitEntity implements Serializable {


    @Serial
    private static final long serialVersionUID = -1724457128791641020L;

    @NotBlank(message = "{app.message.failure.blank}")
    @NotNull(message = "{app.message.failure.blank}")
    @Column(nullable = false)
    private String merchantId;

}
