package com.example.springdemo.modal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModalRepository extends JpaRepository<Modal, Long> {
    boolean existsByEmail(String email);

    Optional<Modal> findByEmail(String email);
}
