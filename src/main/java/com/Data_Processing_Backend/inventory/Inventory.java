package com.Data_Processing_Backend.inventory;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "inventory", uniqueConstraints = @UniqueConstraint(name = "uk_inventory_code_batch", columnNames = {"entry_code", "batch_number"}))
public class Inventory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "entry_code", nullable = false, length = 100) private String entryCode;
    @Column(nullable = false, length = 255) private String name;
    @Column(name = "batch_number", nullable = false, length = 100) private String batchNumber;
    @Column(nullable = false) private int quantity;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal price;
    @Column(name = "expiry_date", nullable = false) private LocalDate expiryDate;
    protected Inventory() {}
    public Inventory(String entryCode, String name, String batchNumber, int quantity, BigDecimal price, LocalDate expiryDate) {
        this.entryCode = entryCode; this.name = name; this.batchNumber = batchNumber; this.quantity = quantity; this.price = price; this.expiryDate = expiryDate;
    }
    public Long getId() { return id; } public String getEntryCode() { return entryCode; } public String getName() { return name; }
    public String getBatchNumber() { return batchNumber; } public int getQuantity() { return quantity; } public BigDecimal getPrice() { return price; } public LocalDate getExpiryDate() { return expiryDate; }
    public void update(String name, int quantity, BigDecimal price, LocalDate expiryDate) { this.name = name; this.quantity = quantity; this.price = price; this.expiryDate = expiryDate; }
}
