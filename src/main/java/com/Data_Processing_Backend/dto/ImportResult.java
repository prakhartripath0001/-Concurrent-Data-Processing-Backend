package com.Data_Processing_Backend.dto;

public record ImportResult(
    long total,
    long valid,
    long invalid,
    long duplicates,
    long durationMillis,
    String errors
) {}
