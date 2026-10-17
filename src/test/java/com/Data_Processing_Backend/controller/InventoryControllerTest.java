package com.Data_Processing_Backend.controller;

import com.Data_Processing_Backend.dto.InventoryRequest;
import com.Data_Processing_Backend.entity.Inventory;
import com.Data_Processing_Backend.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InventoryControllerTest {

    @Autowired
    private InventoryController controller;

    @Autowired
    private InventoryRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void createAndGetInventory() {
        InventoryRequest request = new InventoryRequest(
                "MED-001", "Amoxicillin", "BATCH-100", 50, new BigDecimal("15.50"), LocalDate.now().plusYears(1)
        );

        ResponseEntity<Inventory> response = controller.create(request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();

        Inventory fetched = controller.get(response.getBody().getId());
        assertThat(fetched.getEntryCode()).isEqualTo("MED-001");
        assertThat(fetched.getName()).isEqualTo("Amoxicillin");
        assertThat(fetched.getQuantity()).isEqualTo(50);
    }
}

