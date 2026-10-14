package com.example.springdemo.controller;

import com.example.springdemo.services.EmailAlreadyExistsException;
import com.example.springdemo.services.InvalidCredentialsException;
import com.example.springdemo.services.Services;
import com.example.springdemo.modal.Modal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.View;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.mockito.Mockito;

class ControllerTests {
    private Services services;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        services = Mockito.mock(Services.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new Controller(services))
                .setViewResolvers((viewName, locale) -> viewName.startsWith("redirect:")
                        ? new RedirectView(viewName.substring("redirect:".length()))
                        : Mockito.mock(View.class))
                .build();
    }

    @Test
    void delegatesValidRegistrationToService() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", " Jane Doe ")
                        .param("email", "JANE@example.com")
                        .param("password", "a-secure-password")
                        .param("terms", "true"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Account created successfully."));

        verify(services).registerUser("Jane Doe", "jane@example.com", "a-secure-password", true);
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        when(services.registerUser("Jane Doe", "jane@example.com", "a-secure-password", true))
                .thenThrow(new EmailAlreadyExistsException("jane@example.com"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Jane Doe")
                        .param("email", "JANE@example.com")
                        .param("password", "a-secure-password")
                        .param("terms", "true"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An account with this email already exists."));
    }

    @Test
    void rejectsRegistrationWhenTermsAreNotAccepted() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Jane Doe")
                        .param("email", "jane@example.com")
                        .param("password", "a-secure-password"))
                .andExpect(status().isBadRequest());

        verify(services, org.mockito.Mockito.never())
                .registerUser(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void logsInAndCreatesSessionForValidCredentials() throws Exception {
        when(services.authenticateUser("jane@example.com", "a-secure-password"))
                .thenReturn(new Modal(1L, "Jane Doe", "jane@example.com", "hashed-password", true));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "jane@example.com")
                        .param("password", "a-secure-password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You are signed in."))
                .andExpect(jsonPath("$.redirect").value("/home"))
                .andExpect(request().sessionAttribute("userEmail", "jane@example.com"));
    }

    @Test
    void displaysWelcomePageForLoggedInSession() throws Exception {
        mockMvc.perform(get("/home").sessionAttr("userEmail", "jane@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("userEmail", "jane@example.com"));
    }

    @Test
    void redirectsToLoginWhenWelcomePageHasNoLoggedInSession() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void rejectsInvalidLoginCredentials() throws Exception {
        when(services.authenticateUser("jane@example.com", "wrong-password"))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "jane@example.com")
                        .param("password", "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }
}
