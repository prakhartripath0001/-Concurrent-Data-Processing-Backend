package com.Data_Processing_Backend.imports;
public record ImportResult(long total, long valid, long invalid, long duplicates, long durationMillis, String errors) {}
