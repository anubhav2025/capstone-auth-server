package com.capstone.authServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capstone.authServer.model.Credential;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {
    Credential findByOwnerAndRepository(String owner, String repository);
}
