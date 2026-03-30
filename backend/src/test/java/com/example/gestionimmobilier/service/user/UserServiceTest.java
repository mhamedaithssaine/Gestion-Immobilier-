package com.example.gestionimmobilier.service.user;

import com.example.gestionimmobilier.dto.user.CreateUserRequest;
import com.example.gestionimmobilier.dto.user.UtilisateurResponse;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.mapper.UserMapper;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import com.example.gestionimmobilier.models.enums.Role;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_echoue_siAucunRole() {
        CreateUserRequest request = new CreateUserRequest(
                "user1",
                "user1@example.com",
                "John",
                "Doe",
                "password",
                List.of() // pas de rôles
        );

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ValidationException.class);

        verifyNoInteractions(keycloakAdminService);
    }

    @Test
    void createUser_ok_quandRolesValides() {
        CreateUserRequest request = new CreateUserRequest(
                "user1",
                "user1@example.com",
                "John",
                "Doe",
                "password",
                List.of(Role.ROLE_CLIENT)
        );

        String keycloakId = UUID.randomUUID().toString();
        when(keycloakAdminService.createUserInKeycloak(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyList(), anyBoolean()))
                .thenReturn(keycloakId);

        Utilisateur utilisateur = mock(Utilisateur.class);
        UUID utilisateurId = UUID.randomUUID();
        lenient().when(utilisateur.getId()).thenReturn(utilisateurId);
        lenient().when(utilisateur.getKeycloakId()).thenReturn(keycloakId);

        when(utilisateurRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(utilisateur));

        UtilisateurResponse response = new UtilisateurResponse(
                utilisateurId,
                keycloakId,
                "user1",
                "user1@example.com",
                "John",
                "Doe",
                List.of(Role.ROLE_CLIENT),
                "CLIENT",
                true,
                true
        );
        when(userMapper.toResponse(utilisateur)).thenReturn(response);

        UtilisateurResponse result = userService.createUser(request);

        assertThat(result.id()).isEqualTo(utilisateur.getId());
        verify(keycloakAdminService).createUserInKeycloak(
                request.username(), request.email(), request.firstName(), request.lastName(),
                request.password(), request.roles(), true);
        verify(keycloakAdminService).syncUserByKeycloakId(keycloakId);
    }

    @Test
    void updateUserEnabled_activeEtDesactiveCorrectement() {
        UUID id = UUID.randomUUID();
        Utilisateur utilisateur = mock(Utilisateur.class);
        lenient().when(utilisateur.getId()).thenReturn(id);
        when(utilisateur.getKeycloakId()).thenReturn("kc-id");
        lenient().when(utilisateur.isEnabled()).thenReturn(false);

        when(utilisateurRepository.findById(id)).thenReturn(Optional.of(utilisateur));
        when(userMapper.toResponse(any(Utilisateur.class))).thenReturn(
                new UtilisateurResponse(
                        id,
                        "kc-id",
                        "user",
                        "user@example.com",
                        "John",
                        "Doe",
                        List.of(Role.ROLE_CLIENT),
                        "CLIENT",
                        true,
                        true
                )
        );

        UtilisateurResponse response = userService.updateUserEnabled(id, true);

        verify(keycloakAdminService).setUserEnabled("kc-id", true);
        verify(utilisateurRepository).save(utilisateur);
        assertThat(response).isNotNull();
        assertThat(response.enabled()).isTrue();
    }
}

