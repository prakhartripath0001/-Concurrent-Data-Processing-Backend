package com.Data_Processing_Backend.imports;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

final class CsvRowValidator {
    private CsvRowValidator() {}
    static CsvRecord parse(String line) {
        List<String> values = parseColumns(line);
        if (values.size() != 6) throw new IllegalArgumentException("expected 6 columns: entryCode,name,batchNumber,quantity,price,expiryDate");
        String code = required(values.get(0), "entryCode"); String name = required(values.get(1), "name"); String batch = required(values.get(2), "batchNumber");
        try {
            int quantity = Integer.parseInt(values.get(3).trim());
            if (quantity < 0) throw new IllegalArgumentException("quantity must be zero or positive");
            BigDecimal price = new BigDecimal(values.get(4).trim());
            if (price.signum() <= 0) throw new IllegalArgumentException("price must be greater than zero");
            LocalDate expiry = LocalDate.parse(values.get(5).trim());
            if (!expiry.isAfter(LocalDate.now())) throw new IllegalArgumentException("expiryDate must be in the future");
            return new CsvRecord(code, name, batch, quantity, price, expiry);
        } catch (NumberFormatException ex) { throw new IllegalArgumentException("quantity must be an integer and price must be numeric"); }
          catch (DateTimeParseException ex) { throw new IllegalArgumentException("expiryDate must use ISO format yyyy-MM-dd"); }
    }
    private static String required(String value, String name) { if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(name + " is required"); return value.trim(); }
    static List<String> parseColumns(String line) {
        List<String> values = new ArrayList<>(); StringBuilder value = new StringBuilder(); boolean quoted = false;
        for (int i = 0; i < line.length(); i++) { char c = line.charAt(i); if (c == '"') { if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') { value.append(c); i++; } else quoted = !quoted; } else if (c == ',' && !quoted) { values.add(value.toString()); value.setLength(0); } else value.append(c); }
        if (quoted) throw new IllegalArgumentException("unclosed quoted value");
        values.add(value.toString()); return values;
    }
}
