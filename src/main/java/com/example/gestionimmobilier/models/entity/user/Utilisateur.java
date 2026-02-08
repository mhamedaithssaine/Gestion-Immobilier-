package com.example.gestionimmobilier.models.entity.user;

import com.example.gestionimmobilier.models.entity.base.BaseEntity;
import com.example.gestionimmobilier.models.enums.Role;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Utilisateur extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String keycloakId;

    @Column(nullable = false, unique = true)
    private String email;

    private String nom;
    private String prenom;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "utilisateur_roles",
            joinColumns = @JoinColumn(name = "utilisateur_id")
    )
    @Enumerated(EnumType.STRING)
    private List<Role> roles;
}
