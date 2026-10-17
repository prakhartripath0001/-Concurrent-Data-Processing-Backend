package com.Data_Processing_Backend.inventory;

import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryRepository repository;
    public InventoryController(InventoryRepository repository) { this.repository = repository; }
    @PostMapping public ResponseEntity<Inventory> create(@Valid @RequestBody InventoryRequest request) {
        if (repository.existsByEntryCodeAndBatchNumber(request.entryCode(), request.batchNumber())) throw new DuplicateInventoryException();
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(toEntity(request)));
    }
    @GetMapping("/{id}") public Inventory get(@PathVariable Long id) { return repository.findById(id).orElseThrow(InventoryNotFoundException::new); }
    @GetMapping public Page<Inventory> search(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "id") String sort,
                                               @RequestParam(defaultValue = "asc") String direction, @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        if (size < 1 || size > 100 || !sort.matches("id|entryCode|name|batchNumber|quantity|price|expiryDate")) throw new IllegalArgumentException("Invalid pagination or sort field");
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return repository.findByEntryCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrBatchNumberContainingIgnoreCase(q, q, q, PageRequest.of(page, size, Sort.by(sortDirection, sort)));
    }
    @PutMapping("/{id}") public Inventory update(@PathVariable Long id, @Valid @RequestBody InventoryRequest request) {
        Inventory inventory = repository.findById(id).orElseThrow(InventoryNotFoundException::new);
        if (!inventory.getEntryCode().equals(request.entryCode()) || !inventory.getBatchNumber().equals(request.batchNumber())) throw new IllegalArgumentException("Entry code and batch number cannot be changed");
        inventory.update(request.name(), request.quantity(), request.price(), request.expiryDate());
        return repository.save(inventory);
    }
    private Inventory toEntity(InventoryRequest r) { return new Inventory(r.entryCode().trim(), r.name().trim(), r.batchNumber().trim(), r.quantity(), r.price(), r.expiryDate()); }
}
