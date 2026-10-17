package com.Data_Processing_Backend.repository;

import com.Data_Processing_Backend.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {
    boolean existsByEntryCodeAndBatchNumber(String entryCode, String batchNumber);
    Page<Inventory> findByEntryCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrBatchNumberContainingIgnoreCase(String code, String name, String batch, Pageable pageable);
}

