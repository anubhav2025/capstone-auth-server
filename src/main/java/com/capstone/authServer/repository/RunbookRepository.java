package com.capstone.authServer.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.authServer.model.runbook.Runbook;

import java.util.List;

public interface RunbookRepository extends JpaRepository<Runbook, Long> {
    List<Runbook> findByTenantId(String tenantId);
}

