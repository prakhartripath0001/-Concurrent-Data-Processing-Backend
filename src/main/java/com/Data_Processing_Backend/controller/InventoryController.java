package com.Data_Processing_Backend.controller;

import com.Data_Processing_Backend.dto.InventoryRequest;
import com.Data_Processing_Backend.entity.Inventory;
import com.Data_Processing_Backend.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Inventory> create(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    public Inventory get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public Page<Inventory> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.search(q, sort, direction, page, size);
    }

    @PutMapping("/{id}")
    public Inventory update(@PathVariable Long id, @Valid @RequestBody InventoryRequest request) {
        return service.update(id, request);
    }
}

