package com.cv.s10coreservice.entity.generic;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@FilterDef(name = "contextFilter", parameters = {
        @ParamDef(name = "unitId", type = String.class)
})
@Filter(name = "contextFilter", condition = "unit_id = :unitId")
public abstract class GenericUnitEntity extends GenericEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 8945882357182749646L;

    @NotBlank(message = "{app.message.failure.blank}")
    @NotNull(message = "{app.message.failure.blank}")
    @Column(nullable = false)
    private String unitId;

}
