package com.example.springdemo.services;

import com.example.springdemo.modal.Modal;
import com.example.springdemo.modal.ModalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ServicesTests {
    private ModalRepository modalRepository;
    private Services services;

    @BeforeEach
    void setUp() {
        modalRepository = mock(ModalRepository.class);
        services = new Services(modalRepository);
    }

    @Test
    void savesUserWithNormalizedEmailAndHashedPasswordWhenEmailIsUnique() {
        when(modalRepository.existsByEmail("jane@example.com")).thenReturn(false);

        services.registerUser(" Jane Doe ", "JANE@example.com", "a-secure-password", true);

        ArgumentCaptor<Modal> savedUser = ArgumentCaptor.forClass(Modal.class);
        verify(modalRepository).save(savedUser.capture());
        assertEquals("Jane Doe", savedUser.getValue().getName());
        assertEquals("jane@example.com", savedUser.getValue().getEmail());
        assertNotEquals("a-secure-password", savedUser.getValue().getPasswordHash());
        assertTrue(savedUser.getValue().isTermsAccepted());
    }

    @Test
    void rejectsDuplicateEmailWithoutSaving() {
        when(modalRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> services.registerUser("Jane Doe", "JANE@example.com", "a-secure-password", true));

        verify(modalRepository, never()).save(any(Modal.class));
    }

    @Test
    void authenticatesUserUsingEmailAndPasswordHash() {
        Modal user = new Modal(1L, "Jane Doe", "jane@example.com",
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("a-secure-password"), true);
        when(modalRepository.findByEmail("jane@example.com")).thenReturn(java.util.Optional.of(user));

        assertEquals(user, services.authenticateUser("JANE@example.com", "a-secure-password"));
    }

    @Test
    void rejectsIncorrectLoginCredentials() {
        when(modalRepository.findByEmail("jane@example.com")).thenReturn(java.util.Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> services.authenticateUser("JANE@example.com", "wrong-password"));
        verify(modalRepository).findByEmail("jane@example.com");
    }
}
