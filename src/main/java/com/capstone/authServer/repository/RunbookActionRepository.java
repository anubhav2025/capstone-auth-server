package com.capstone.authServer.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.authServer.model.runbook.Runbook;
import com.capstone.authServer.model.runbook.RunbookAction;

import java.util.List;

public interface RunbookActionRepository extends JpaRepository<RunbookAction, Long> {
    List<RunbookAction> findByRunbook(Runbook runbook);
}

