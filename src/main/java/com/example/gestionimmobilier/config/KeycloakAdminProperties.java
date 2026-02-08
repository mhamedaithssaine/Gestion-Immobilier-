package com.example.gestionimmobilier.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "keycloak.admin")
public class KeycloakAdminProperties {

    private String serverUrl = "http://localhost:8180";
    private String realm = "master";
    private String clientId = "admin-cli";
    private String username = "admin";
    private String password;
    private String realmUsers = "gestion-immobilier";

    public String getServerUrl() { return serverUrl; }
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
    public String getRealm() { return realm; }
    public void setRealm(String realm) { this.realm = realm; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealmUsers() { return realmUsers; }
    public void setRealmUsers(String realmUsers) { this.realmUsers = realmUsers; }
}