package com.Data_Processing_Backend.imports;

import com.Data_Processing_Backend.inventory.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImportWorker {
    private final InventoryRepository inventoryRepository;
    public ImportWorker(InventoryRepository inventoryRepository) { this.inventoryRepository = inventoryRepository; }
    @Transactional
    public boolean insert(CsvRecord row) {
        if (inventoryRepository.existsByEntryCodeAndBatchNumber(row.code(), row.batch())) return false;
        try {
            inventoryRepository.saveAndFlush(new Inventory(row.code(), row.name(), row.batch(), row.quantity(), row.price(), row.expiry()));
            return true;
        } catch (DataIntegrityViolationException ex) { return false; }
    }
}
