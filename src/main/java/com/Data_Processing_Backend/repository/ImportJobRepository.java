package com.Data_Processing_Backend.repository;

import com.Data_Processing_Backend.entity.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportJobRepository extends JpaRepository<ImportJob, String> {
}
