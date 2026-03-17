package com.example.gestionimmobilier.controller;

import com.example.gestionimmobilier.config.CustomAuthenticationEntryPoint;
import com.example.gestionimmobilier.service.BienService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BienImmobilierController.class)
class BienImmobilierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BienService bienService;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Test
    void listerMesBiens_retourne200_avecJwtProprietaire() throws Exception {
        Mockito.when(bienService.listerBiens("keycloak-proprio-1"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/proprietaire/biens")
                        .with(jwt().jwt(jwt -> jwt.subject("keycloak-proprio-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));
    }

}

