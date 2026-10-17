package com.Data_Processing_Backend.controller;

import com.Data_Processing_Backend.entity.ImportJob;
import com.Data_Processing_Backend.entity.enums.JobStatus;
import com.Data_Processing_Backend.entity.enums.ProcessingMode;
import com.Data_Processing_Backend.repository.ImportJobRepository;
import com.Data_Processing_Backend.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ImportControllerTest {

    @Autowired
    private ImportController controller;

    @Autowired
    private ImportJobRepository jobRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
        inventoryRepository.deleteAll();
    }

    @Test
    void uploadAndGetImportJob() {
        String csv = """
                entryCode,name,batchNumber,quantity,price,expiryDate
                MED-100,Amoxicillin,B100,10,12.50,2030-01-01
                """;
        MockMultipartFile file = new MockMultipartFile("file", "inventory.csv", "text/csv", csv.getBytes());

        ResponseEntity<ImportJob> response = controller.upload(file, ProcessingMode.SEQUENTIAL);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(JobStatus.COMPLETED);
        assertThat(response.getBody().getSuccessfulRecords()).isEqualTo(1);

        ImportJob fetched = controller.get(response.getBody().getId());
        assertThat(fetched.getId()).isEqualTo(response.getBody().getId());
        assertThat(fetched.getStatus()).isEqualTo(JobStatus.COMPLETED);
    }
}
