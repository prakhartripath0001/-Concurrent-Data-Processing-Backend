package com.Data_Processing_Backend.imports;
import java.math.BigDecimal;
import java.time.LocalDate;
record CsvRecord(String code, String name, String batch, int quantity, BigDecimal price, LocalDate expiry) {}
