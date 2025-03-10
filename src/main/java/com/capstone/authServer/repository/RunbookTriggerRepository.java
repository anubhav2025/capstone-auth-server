package com.capstone.authServer.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.authServer.model.runbook.Runbook;
import com.capstone.authServer.model.runbook.RunbookTrigger;

import java.util.List;

public interface RunbookTriggerRepository extends JpaRepository<RunbookTrigger, Long> {
    List<RunbookTrigger> findByRunbook(Runbook runbook);
}
