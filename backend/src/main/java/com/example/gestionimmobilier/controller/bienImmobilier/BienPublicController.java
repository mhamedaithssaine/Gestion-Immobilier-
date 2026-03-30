package com.example.gestionimmobilier.controller.bienImmobilier;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.immobilier.BienResponse;
import com.example.gestionimmobilier.mapper.BienMapper;
import com.example.gestionimmobilier.repository.BienImmobilierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/public/biens")
public class BienPublicController {

    private final BienImmobilierRepository bienRepository;
    private final BienMapper bienMapper;

    public BienPublicController(BienImmobilierRepository bienRepository, BienMapper bienMapper) {
        this.bienRepository = bienRepository;
        this.bienMapper = bienMapper;
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiRetour<Page<BienResponse>>> listerBiensDisponibles(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) BigDecimal minPrix,
            @RequestParam(required = false) BigDecimal maxPrix,
            @RequestParam(required = false) Double minSurface,
            @RequestParam(required = false) Double maxSurface,
            @RequestParam(required = false) List<String> types,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        boolean wantAppartement = false;
        boolean wantMaison = false;
        if (types != null) {
            for (String raw : types) {
                if (raw == null || raw.isBlank()) {
                    continue;
                }
                String t = raw.trim().toUpperCase(Locale.ROOT);
                if ("APPARTEMENT".equals(t)) {
                    wantAppartement = true;
                } else if ("MAISON".equals(t)) {
                    wantMaison = true;
                }
            }
        }
        boolean noTypeFilter = !wantAppartement && !wantMaison;
        Page<BienResponse> data = bienRepository
                .searchDisponibles(
                        q, ville, minPrix, maxPrix, minSurface, maxSurface,
                        noTypeFilter, wantAppartement, wantMaison, pageable)
                .map(bienMapper::toBienResponse);
        return ResponseEntity.ok(ApiRetour.success("Liste des biens disponibles", data));
    }
}

