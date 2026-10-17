package com.Data_Processing_Backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CsvRecord(
    String code,
    String name,
    String batch,
    int quantity,
    BigDecimal price,
    LocalDate expiry
) {}
