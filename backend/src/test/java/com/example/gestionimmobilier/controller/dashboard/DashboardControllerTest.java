package com.example.gestionimmobilier.controller.dashboard;

import com.example.gestionimmobilier.config.CustomAuthenticationEntryPoint;
import com.example.gestionimmobilier.dto.dashboard.*;
import com.example.gestionimmobilier.service.dashboard.DashboardService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void getNombreBiensDisponibles_retourne200() throws Exception {
        Mockito.when(dashboardService.getNombreBiensDisponibles())
                .thenReturn(new NombreBiensDisponiblesResponse(5L));

        mockMvc.perform(get("/api/admin/dashboard/nombre-biens-disponibles")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombreBiensDisponibles").value(5));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_AGENT"})
    void getRevenusMensuels_retourne200() throws Exception {
        Mockito.when(dashboardService.getRevenusMensuels(anyInt(), anyInt()))
                .thenReturn(new RevenusMensuelsResponse(2026, 3, BigDecimal.TEN));

        mockMvc.perform(get("/api/admin/dashboard/revenus-mensuels")
                        .param("annee", "2026")
                        .param("mois", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.revenusMensuels").value(10));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void getStatistiquesMandatsToutesAgences_retourne200() throws Exception {
        UUID agenceId = UUID.randomUUID();
        MandatsGestionStatistiqueResponse resp =
                new MandatsGestionStatistiqueResponse(agenceId, "Agence", 1L, 1L, 0L, 0L, 0L);
        Mockito.when(dashboardService.getStatistiquesMandatsToutesAgences())
                .thenReturn(List.of(resp));

        mockMvc.perform(get("/api/admin/dashboard/agences/stats/mandats-gestion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].agenceId").value(agenceId.toString()));
    }
}

