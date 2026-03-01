package com.example.gestionimmobilier.service;

import com.example.gestionimmobilier.config.KeycloakAdminProperties;
import com.example.gestionimmobilier.dto.keycloack.KeycloakUserResponse;
import com.example.gestionimmobilier.exception.*;
import com.example.gestionimmobilier.mapper.KeycloakUserMapper;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import com.example.gestionimmobilier.models.enums.Role;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.gestionimmobilier.exception.ErrorMessages.UTILISATEUR_EXISTE_DEJA;

@Service
public class KeycloakAdminService {

    private final KeycloakAdminProperties props;
    private final RestTemplate restTemplate;
    private final UtilisateurRepository utilisateurRepository;
    private final KeycloakUserMapper keycloakUserMapper;

    public KeycloakAdminService(KeycloakAdminProperties props,
                                RestTemplate restTemplate,
                                UtilisateurRepository utilisateurRepository,
                                KeycloakUserMapper keycloakUserMapper) {
        this.props = props;
        this.restTemplate = restTemplate;
        this.utilisateurRepository = utilisateurRepository;
        this.keycloakUserMapper = keycloakUserMapper;
    }

    public String getAdminToken() {
        String url = buildTokenUrl();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(buildTokenBody(), buildFormHeaders());
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {}
        );
        return extractAccessToken(response.getBody());
    }

    public List<KeycloakUserResponse> getUsersFromKeycloak() {
        String token = getAdminToken();
        String url = buildUsersUrl();
        HttpEntity<Void> request = new HttpEntity<>(buildAuthHeaders(token));
        ResponseEntity<List<KeycloakUserResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {}
        );
        return Optional.ofNullable(response.getBody()).orElse(List.of());
    }

    public List<Role> getUserRealmRoles(String keycloakUserId) {
        String token = getAdminToken();
        String url = buildUserRolesUrl(keycloakUserId);
        HttpEntity<Void> request = new HttpEntity<>(buildAuthHeaders(token));
        ResponseEntity<List<Map<String, String>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {}
        );
        return mapToRoles(response.getBody());
    }

    @Transactional
    public void syncUsersToDatabase() {
        List<KeycloakUserResponse> keycloakUsers = getUsersFromKeycloak();
        for (KeycloakUserResponse kcUser : keycloakUsers) {
            syncUser(kcUser);
        }
    }

    private void syncUser(KeycloakUserResponse kcUser) {
        List<Role> roles = getUserRealmRoles(kcUser.id());
        Optional<Utilisateur> existing = utilisateurRepository.findByKeycloakId(kcUser.id());
        Utilisateur utilisateur = existing
                .map(u -> updateFromKeycloak(u, kcUser, roles))
                .orElseGet(() -> createFromKeycloak(kcUser, roles));
        utilisateurRepository.save(utilisateur);
    }

    private Utilisateur createFromKeycloak(KeycloakUserResponse kcUser, List<Role> roles) {
        if (roles.contains(Role.ROLE_ADMIN)) {
            return keycloakUserMapper.toAdmin(kcUser, roles);
        }
        if (roles.contains(Role.ROLE_AGENT)) {
            return keycloakUserMapper.toAgent(kcUser, roles);
        }
        if (roles.contains(Role.ROLE_CLIENT)) {
            return keycloakUserMapper.toClient(kcUser, roles);
        }
        return keycloakUserMapper.toProprietaire(kcUser, roles);
    }

    private Utilisateur updateFromKeycloak(Utilisateur u, KeycloakUserResponse kcUser, List<Role> roles) {
        u.setUsername(kcUser.username());
        u.setEmail(normalizeEmail(kcUser));
        u.setFirstName(kcUser.firstName());
        u.setLastName(kcUser.lastName());
        u.setRoles(roles);
        u.setEmailVerified(kcUser.emailVerified());
        return u;
    }

    private String normalizeEmail(KeycloakUserResponse kcUser) {
        if (kcUser.email() != null && !kcUser.email().isBlank()) {
            return kcUser.email();
        }
        return kcUser.username() + "@keycloak.local";
    }

    private List<Role> mapToRoles(List<Map<String, String>> roleMaps) {
        if (roleMaps == null) {
            return List.of();
        }
        return roleMaps.stream()
                .map(m -> m.get("name"))
                .filter(name -> name != null && name.startsWith("ROLE_"))
                .map(this::toRole)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<Role> toRole(String name) {
        try {
            return Optional.of(Role.valueOf(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private String buildTokenUrl() {
        return props.getServerUrl() + "/realms/" + props.getRealm() + "/protocol/openid-connect/token";
    }

    private String buildUsersUrl() {
        return props.getServerUrl() + "/admin/realms/" + props.getRealmUsers() + "/users";
    }

    private String buildUserRolesUrl(String userId) {
        return props.getServerUrl() + "/admin/realms/" + props.getRealmUsers() + "/users/" + userId + "/role-mappings/realm";
    }

    private HttpHeaders buildFormHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private HttpHeaders buildAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private MultiValueMap<String, String> buildTokenBody() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", props.getClientId());
        body.add("username", props.getUsername());
        body.add("password", props.getPassword());
        return body;
    }

    private String extractAccessToken(Map<String, Object> tokenResponse) {
        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new RuntimeException("Impossible d'obtenir le token admin Keycloak");
        }
        return (String) tokenResponse.get("access_token");
    }

    public List<Map<String,Object>> getRealmRoles(){
        String token = getAdminToken();
        String url = props.getServerUrl() + "/admin/realms/" + props.getRealmUsers() + "/roles";
        HttpEntity<Void> request = new HttpEntity<>(buildAuthHeaders(token));
        ResponseEntity<List<Map<String,Object>>> response = restTemplate.exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<>() {}
        );

        return Optional.ofNullable(response.getBody()).orElse(List.of());
    }

    // Assigne les rôles à un utilisateur Keycloak
    @Transactional
    public void assignRolesToUser(String keycloakUserId, List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new ValidationException(ErrorMessages.LISTE_ROLES_VIDE);
        }

        List<Map<String, Object>> realmRoles = getRealmRoles();
        List<Map<String, Object>> rolesToAssign = roles.stream()
                .map(Role::name)
                .map(roleName -> findRealmRoleByName(realmRoles, roleName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (rolesToAssign.isEmpty()) {
            throw new ValidationException(ErrorMessages.AUCUN_ROLE_VALIDE + roles);
        }

        String token = getAdminToken();
        String url = buildUserRolesUrl(keycloakUserId);
        HttpHeaders headers = buildAuthHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1. Supprimer les rôles actuels
        List<Map<String, Object>> currentRoles = getKeycloakUserRoles(keycloakUserId);
        if (!currentRoles.isEmpty()) {
            HttpEntity<List<Map<String, Object>>> deleteRequest = new HttpEntity<>(currentRoles, headers);
            restTemplate.exchange(url, HttpMethod.DELETE, deleteRequest, Void.class);
        }

        // 2. Attribuer les nouveaux rôles
        HttpEntity<List<Map<String, Object>>> postRequest = new HttpEntity<>(rolesToAssign, headers);
        restTemplate.exchange(url, HttpMethod.POST, postRequest, Void.class);
    }

    private List<Map<String, Object>> getKeycloakUserRoles(String keycloakUserId) {
        String token = getAdminToken();
        String url = buildUserRolesUrl(keycloakUserId);
        HttpEntity<Void> request = new HttpEntity<>(buildAuthHeaders(token));
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {}
        );
        return Optional.ofNullable(response.getBody()).orElse(List.of());
    }

    private Optional<Map<String, Object>> findRealmRoleByName(List<Map<String, Object>> realmRoles, String roleName) {
        return realmRoles.stream()
                .filter(r -> roleName.equals(r.get("name")))
                .findFirst();
    }

    private String buildUserUrl(String keycloakUserId) {
        return buildUsersUrl() + "/" + keycloakUserId;
    }

    public void setUserEnabled(String keycloakUserId, boolean enabled) {
        String token = getAdminToken();
        String url = buildUserUrl(keycloakUserId);

        HttpEntity<Void> getRequest = new HttpEntity<>(buildAuthHeaders(token));
        ResponseEntity<Map<String, Object>> getResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                getRequest,
                new ParameterizedTypeReference<>() {}
        );

        Map<String, Object> user = getResponse.getBody();
        if (user == null) {
            throw new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE_KEYCLOAK);
        }

        user.put("enabled", enabled);

        HttpHeaders headers = buildAuthHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> putRequest = new HttpEntity<>(user, headers);
        restTemplate.exchange(url, HttpMethod.PUT, putRequest, Void.class);
    }


    public String createUserInKeycloak(String username, String email, String firstName, String lastName, String password, List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new ValidationException(ErrorMessages.AUCUN_ROLE_VALIDE);
        }

        String token = getAdminToken();
        String url = buildUsersUrl();
        HttpHeaders headers = buildAuthHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> userBody = Map.of(
                "username", username,
                "email", email,
                "firstName", firstName,
                "lastName", lastName,
                "enabled", true,
                "emailVerified", true
        );

        HttpEntity<Map<String, Object>> createRequest = new HttpEntity<>(userBody, headers);
        ResponseEntity<Void> createResponse;
        try {
            createResponse = restTemplate.exchange(url, HttpMethod.POST, createRequest, Void.class);
        } catch (HttpClientErrorException.Conflict e) {
            throw new BusinessRuleException(UTILISATEUR_EXISTE_DEJA);
        }

        URI location = createResponse.getHeaders().getLocation();
        if (location == null) {
            throw new InternalServerException(ErrorMessages.ERREUR_ID_KEYCLOAK);
        }
        String path = location.getPath();
        String keycloakUserId = path.substring(path.lastIndexOf('/') + 1);

        String resetPasswordUrl = buildUserUrl(keycloakUserId) + "/reset-password";
        Map<String, Object> credentials = Map.of(
                "type", "password",
                "value", password,
                "temporary", false
        );
        HttpEntity<Map<String, Object>> passwordRequest = new HttpEntity<>(credentials, headers);
        restTemplate.exchange(resetPasswordUrl, HttpMethod.PUT, passwordRequest, Void.class);

        assignRolesToUser(keycloakUserId, roles);

        return keycloakUserId;
    }


    @Transactional
    public void syncUserByKeycloakId(String keycloakUserId) {
        String token = getAdminToken();
        String url = buildUserUrl(keycloakUserId);
        HttpEntity<Void> request = new HttpEntity<>(buildAuthHeaders(token));
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {}
        );
        Map<String, Object> kcUser = response.getBody();
        if (kcUser == null) {
            throw new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE_KEYCLOAK);
        }

        KeycloakUserResponse kcUserResponse = mapToKeycloakUserResponse(kcUser);
        List<Role> roles = getUserRealmRoles(keycloakUserId);
        syncUser(kcUserResponse, roles);
    }

    private void syncUser(KeycloakUserResponse kcUser, List<Role> roles) {
        Optional<Utilisateur> existing = utilisateurRepository.findByKeycloakId(kcUser.id());
        Utilisateur utilisateur = existing
                .map(u -> updateFromKeycloak(u, kcUser, roles))
                .orElseGet(() -> createFromKeycloak(kcUser, roles));
        utilisateurRepository.save(utilisateur);
    }

    private KeycloakUserResponse mapToKeycloakUserResponse(Map<String, Object> m) {
        Object emailVerifiedObj = m.get("emailVerified");
        if (emailVerifiedObj == null) {
            emailVerifiedObj = m.get("email_verified");
        }
        boolean emailVerified = Boolean.TRUE.equals(emailVerifiedObj);
        return new KeycloakUserResponse(
                (String) m.get("id"),
                (String) m.get("username"),
                (String) m.get("email"),
                (String) m.get("firstName"),
                (String) m.get("lastName"),
                Boolean.TRUE.equals(m.get("enabled")),
                emailVerified,
                m.get("createdTimestamp") != null ? ((Number) m.get("createdTimestamp")).longValue() : null
        );
    }

}