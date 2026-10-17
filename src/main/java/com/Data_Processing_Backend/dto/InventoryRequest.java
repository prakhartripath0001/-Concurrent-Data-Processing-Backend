package com.Data_Processing_Backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryRequest(
    @NotBlank String entryCode,
    @NotBlank String name,
    @NotBlank String batchNumber,
    @NotNull @PositiveOrZero Integer quantity,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
    @NotNull @Future LocalDate expiryDate
) {}

