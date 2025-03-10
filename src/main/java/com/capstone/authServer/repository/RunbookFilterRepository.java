package com.capstone.authServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.authServer.model.runbook.Runbook;
import com.capstone.authServer.model.runbook.RunbookFilter;

import java.util.List;

public interface RunbookFilterRepository extends JpaRepository<RunbookFilter, Long> {
    List<RunbookFilter> findByRunbook(Runbook runbook);
}

