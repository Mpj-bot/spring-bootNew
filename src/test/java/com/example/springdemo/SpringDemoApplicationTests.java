package com.example.springdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SpringDemoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void homePageAndStaticAssetsAreServed() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Welcome back")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Don't have an account?")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"login-form\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<dialog")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"email\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/css/style.css")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/js/main.js")));

        mockMvc.perform(get("/css/style.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));

        mockMvc.perform(get("/js/main.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/javascript"));

        mockMvc.perform(get("/images/welcome.svg"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("image/svg+xml"));
    }
}
