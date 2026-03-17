package com.example.gestionimmobilier.controller.mandat;

import com.example.gestionimmobilier.dto.contrat.CreateMandatRequest;
import com.example.gestionimmobilier.dto.contrat.MandatResponse;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import com.example.gestionimmobilier.service.mandat.MandatService;
import com.example.gestionimmobilier.config.CustomAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MandatController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class MandatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MandatService mandatService;

    // requis par SecurityConfig.securityFilterChain
    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = {"ROLE_AGENT"})
    void creerMandat_retourne201() throws Exception {
        UUID id = UUID.randomUUID();
        MandatResponse response = new MandatResponse(
                id, "MANDAT-1",
                UUID.randomUUID(), "Proprio Test",
                UUID.randomUUID(), "Agent Test",
                UUID.randomUUID(), "REF-BIEN",
                null, null,
                BigDecimal.TEN,
                StatutMandat.ACTIF,
                null, null
        );

        Mockito.when(mandatService.creerMandat(any(CreateMandatRequest.class))).thenReturn(response);

        CreateMandatRequest request = new CreateMandatRequest(
                response.bienId(),
                response.proprietaireId(),
                response.agentId(),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                BigDecimal.TEN,
                null,
                null
        );

        mockMvc.perform(post("/api/admin/mandats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_AGENT"})
    void listerMandats_retourne200() throws Exception {
        Mockito.when(mandatService.listerMandats(any(), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/admin/mandats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_AGENT"})
    void getMandat_retourne200() throws Exception {
        UUID id = UUID.randomUUID();
        MandatResponse response = Mockito.mock(MandatResponse.class);
        Mockito.when(response.id()).thenReturn(id);
        Mockito.when(mandatService.getMandatById(any())).thenReturn(response);

        mockMvc.perform(get("/api/admin/mandats/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_AGENT"})
    void resilierMandat_retourne200() throws Exception {
        UUID id = UUID.randomUUID();
        MandatResponse response = Mockito.mock(MandatResponse.class);
        Mockito.when(response.id()).thenReturn(id);
        Mockito.when(mandatService.resilierMandat(any())).thenReturn(response);

        mockMvc.perform(patch("/api/admin/mandats/{id}/resilier", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }
}

