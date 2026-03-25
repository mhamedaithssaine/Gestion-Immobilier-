package com.example.gestionimmobilier.service.finance;

import com.example.gestionimmobilier.dto.finance.CreateVersementRequest;
import com.example.gestionimmobilier.dto.finance.VersementResponse;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.models.entity.contrat.Bail;
import com.example.gestionimmobilier.models.entity.finance.Versement;
import com.example.gestionimmobilier.models.enums.StatutBail;
import com.example.gestionimmobilier.repository.BailRepository;
import com.example.gestionimmobilier.repository.VersementRepository;
import com.example.gestionimmobilier.service.storage.CloudStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VersementServiceTest {

    @Mock
    private VersementRepository versementRepository;
    @Mock
    private BailRepository bailRepository;
    @Mock
    private QuittanceService quittanceService;
    @Mock
    private CloudStorageService cloudStorageService;

    private VersementService versementService;

    @BeforeEach
    void setUp() {
        versementService = new VersementService(
                versementRepository,
                bailRepository,
                quittanceService,
                cloudStorageService
        );
    }

    @Test
    void createVersement_ok_quandBailActif() {
        UUID bailId = UUID.randomUUID();
        Bail bail = new Bail();
        bail.setId(bailId);
        bail.setStatut(StatutBail.ACTIF);

        when(bailRepository.findById(bailId)).thenReturn(Optional.of(bail));
        when(versementRepository.existsByReferencePaiement("REF-1")).thenReturn(false);

        CreateVersementRequest request = new CreateVersementRequest(
                bailId,
                BigDecimal.valueOf(1000),
                com.example.gestionimmobilier.models.enums.ModeVersement.VIREMENT,
                "REF-1",
                null
        );

        Versement saved = Versement.builder()
                .id(UUID.randomUUID())
                .bail(bail)
                .montant(request.montant())
                .build();

        when(versementRepository.save(any(Versement.class))).thenReturn(saved);

        VersementResponse response = versementService.createVersement(request, (MultipartFile) null);

        assertThat(response.bailId()).isEqualTo(bailId);

        ArgumentCaptor<Versement> captor = ArgumentCaptor.forClass(Versement.class);
        verify(versementRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getMontant()).isEqualByComparingTo(request.montant());
    }

    @Test
    void createVersement_ko_siBailNonActif() {
        UUID bailId = UUID.randomUUID();
        Bail bail = new Bail();
        bail.setId(bailId);
        bail.setStatut(StatutBail.RESILIE);

        when(bailRepository.findById(bailId)).thenReturn(Optional.of(bail));

        CreateVersementRequest request = new CreateVersementRequest(
                bailId,
                BigDecimal.valueOf(500),
                com.example.gestionimmobilier.models.enums.ModeVersement.VIREMENT,
                "REF-2",
                null
        );

        assertThatThrownBy(() -> versementService.createVersement(request, null))
                .isInstanceOf(ValidationException.class);
    }
}

