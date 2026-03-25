package com.example.gestionimmobilier.controller.finance;

import com.example.gestionimmobilier.config.CustomAuthenticationEntryPoint;
import com.example.gestionimmobilier.dto.finance.CreateVersementRequest;
import com.example.gestionimmobilier.dto.finance.VersementResponse;
import com.example.gestionimmobilier.service.finance.VersementService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VersementController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class VersementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VersementService versementService;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = {"ROLE_AGENT"})
    void createVersementJson_retourne201() throws Exception {
        UUID bailId = UUID.randomUUID();
        CreateVersementRequest request = new CreateVersementRequest(
                bailId,
                BigDecimal.valueOf(1000),
                com.example.gestionimmobilier.models.enums.ModeVersement.VIREMENT,
                "REF-1",
                LocalDateTime.now()
        );

        VersementResponse response = new VersementResponse(
                UUID.randomUUID(),
                bailId,
                "CONTRAT-1",
                LocalDateTime.now(),
                request.montant(),
                request.mode(),
                request.referencePaiement(),
                null,
                true,
                null,
                null,
                null
        );

        Mockito.when(versementService.createVersement(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/admin/versements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bailId").value(bailId.toString()));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_AGENT"})
    void getVersementsByContrat_retourne200() throws Exception {
        UUID contratId = UUID.randomUUID();
        VersementResponse resp = Mockito.mock(VersementResponse.class);
        Mockito.when(versementService.getVersementsByContrat(any()))
                .thenReturn(List.of(resp));

        mockMvc.perform(get("/api/admin/versements/contrats/{contratId}/versements", contratId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));
    }
}

