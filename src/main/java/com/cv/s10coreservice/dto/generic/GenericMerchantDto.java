package com.cv.s10coreservice.dto.generic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class GenericMerchantDto extends GenericUnitDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 3259456727984120813L;

    @NotBlank(message = "{app.message.failure.blank}")
    @NotNull(message = "{app.message.failure.blank}")
    private String merchantId;

}
