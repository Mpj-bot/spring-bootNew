package com.example.springdemo.services;

import com.example.springdemo.modal.Modal;
import com.example.springdemo.modal.ModalRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class Services {
    private final ModalRepository modalRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Services(ModalRepository modalRepository) {
        this.modalRepository = modalRepository;
    }

    public Modal registerUser(String name, String email, String password, boolean termsAccepted) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (modalRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        Modal user = new Modal(
                null,
                name.trim(),
                normalizedEmail,
                passwordEncoder.encode(password),
                termsAccepted
        );
        return modalRepository.save(user);
    }

    public Modal authenticateUser(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        Optional<Modal> user = modalRepository.findByEmail(normalizedEmail);

        if (user.isEmpty() || !passwordEncoder.matches(password, user.get().getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user.get();
    }
}
