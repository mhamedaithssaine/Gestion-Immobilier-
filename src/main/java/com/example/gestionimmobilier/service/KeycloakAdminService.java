package com.example.gestionimmobilier.service;

import com.example.gestionimmobilier.config.KeycloakAdminProperties;
import com.example.gestionimmobilier.dto.keycloack.KeycloakUserResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakAdminService {

    private final KeycloakAdminProperties props;
    private final RestTemplate restTemplate;

    public KeycloakAdminService(KeycloakAdminProperties props, RestTemplate restTemplate) {
        this.props = props;
        this.restTemplate = restTemplate;
    }

    public String getAdminToken() {
        String url = props.getServerUrl() + "/realms/" + props.getRealm() + "/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", props.getClientId());
        body.add("username", props.getUsername());
        body.add("password", props.getPassword());
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, request,
                new ParameterizedTypeReference<>() {});
        Map<String, Object> tokenResponse = response.getBody();
        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new RuntimeException("Impossible d'obtenir le token admin Keycloak");
        }
        return (String) tokenResponse.get("access_token");
    }

    public List<KeycloakUserResponse> getUsers() {
        String token = getAdminToken();
        String url = props.getServerUrl() + "/admin/realms/" + props.getRealmUsers() + "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<List<KeycloakUserResponse>> response = restTemplate.exchange(url, HttpMethod.GET, request,
                new ParameterizedTypeReference<>() {});
        return response.getBody() != null ? response.getBody() : List.of();
    }
}