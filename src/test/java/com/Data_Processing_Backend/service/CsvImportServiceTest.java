package com.Data_Processing_Backend.service;

import com.Data_Processing_Backend.dto.ImportResult;
import com.Data_Processing_Backend.entity.enums.ProcessingMode;
import com.Data_Processing_Backend.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CsvImportServiceTest {

    @Autowired
    private CsvImportService service;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void clearDatabase() {
        inventoryRepository.deleteAll();
    }

    @Test
    void sequentialImportSeparatesValidInvalidAndDuplicates() {
        ImportResult result = service.process(file("""
                entryCode,name,batchNumber,quantity,price,expiryDate
                MED-1,Aspirin,B1,10,12.50,2030-01-01
                MED-1,Aspirin,B1,10,12.50,2030-01-01
                MED-2,Paracetamol,B2,-1,4.00,2030-01-01
                MED-3,Ibuprofen,B3,5,2.00,2030-01-01
                """), ProcessingMode.SEQUENTIAL);

        assertThat(result.total()).isEqualTo(4);
        assertThat(result.valid()).isEqualTo(2);
        assertThat(result.invalid()).isEqualTo(1);
        assertThat(result.duplicates()).isEqualTo(1);
        assertThat(result.errors()).contains("4: quantity must be zero or positive");
        assertThat(inventoryRepository.count()).isEqualTo(2);
    }

    @Test
    void parallelHasSameOutputAsSequentialForFreshDataset() {
        String csv = """
                entryCode,name,batchNumber,quantity,price,expiryDate
                MED-10,A,B1,1,1.00,2030-01-01
                MED-11,B,B2,2,2.00,2030-01-01
                MED-10,A,B1,1,1.00,2030-01-01
                MED-12,C,B3,0,3.00,2030-01-01
                MED-13,D,B4,bad,3.00,2030-01-01
                """;
        ImportResult sequential = service.process(file(csv), ProcessingMode.SEQUENTIAL);
        inventoryRepository.deleteAll();
        ImportResult parallel = service.process(file(csv), ProcessingMode.PARALLEL);
        assertThat(parallel.total()).isEqualTo(sequential.total());
        assertThat(parallel.valid()).isEqualTo(sequential.valid());
        assertThat(parallel.invalid()).isEqualTo(sequential.invalid());
        assertThat(parallel.duplicates()).isEqualTo(sequential.duplicates());
    }

    private MockMultipartFile file(String content) {
        return new MockMultipartFile("file", "inventory.csv", "text/csv", content.getBytes());
    }
}
