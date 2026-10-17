package com.Data_Processing_Backend.service;

import com.Data_Processing_Backend.dto.InventoryRequest;
import com.Data_Processing_Backend.entity.Inventory;
import com.Data_Processing_Backend.exception.DuplicateInventoryException;
import com.Data_Processing_Backend.exception.InventoryNotFoundException;
import com.Data_Processing_Backend.repository.InventoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Inventory create(InventoryRequest request) {
        if (repository.existsByEntryCodeAndBatchNumber(request.entryCode(), request.batchNumber())) {
            throw new DuplicateInventoryException();
        }
        return repository.save(toEntity(request));
    }

    @Transactional(readOnly = true)
    public Inventory get(Long id) {
        return repository.findById(id).orElseThrow(InventoryNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Page<Inventory> search(String q, String sort, String direction, int page, int size) {
        if (size < 1 || size > 100 || !sort.matches("id|entryCode|name|batchNumber|quantity|price|expiryDate")) {
            throw new IllegalArgumentException("Invalid pagination or sort field");
        }
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return repository.findByEntryCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrBatchNumberContainingIgnoreCase(
                q, q, q, PageRequest.of(page, size, Sort.by(sortDirection, sort))
        );
    }

    @Transactional
    public Inventory update(Long id, InventoryRequest request) {
        Inventory inventory = repository.findById(id).orElseThrow(InventoryNotFoundException::new);
        if (!inventory.getEntryCode().equals(request.entryCode()) || !inventory.getBatchNumber().equals(request.batchNumber())) {
            throw new IllegalArgumentException("Entry code and batch number cannot be changed");
        }
        inventory.update(request.name(), request.quantity(), request.price(), request.expiryDate());
        return repository.save(inventory);
    }

    private Inventory toEntity(InventoryRequest r) {
        return new Inventory(r.entryCode().trim(), r.name().trim(), r.batchNumber().trim(), r.quantity(), r.price(), r.expiryDate());
    }
}

