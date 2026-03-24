package com.example.gestionimmobilier.service.auth.register;

import com.example.gestionimmobilier.dto.auth.register.RegisterRequest;
import com.example.gestionimmobilier.dto.user.CreateUserRequest;
import com.example.gestionimmobilier.dto.user.UtilisateurResponse;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.models.enums.Role;
import com.example.gestionimmobilier.service.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthRegisterService {

    private final UserService userService;

    public AuthRegisterService(UserService userService) {
        this.userService = userService;
    }

    public UtilisateurResponse register(RegisterRequest request) {
        List<Role> roles = normalizeAndValidateRoles(request.roles());
        CreateUserRequest createUserRequest = new CreateUserRequest(
                request.username(),
                request.email(),
                request.firstName(),
                request.lastName(),
                request.password(),
                roles
        );
        return userService.createUser(createUserRequest, false);
    }

    private List<Role> normalizeAndValidateRoles(List<Role> requestedRoles) {
        List<Role> roles = (requestedRoles == null || requestedRoles.isEmpty())
                ? List.of(Role.ROLE_CLIENT)
                : requestedRoles;

        boolean containsForbidden = roles.stream()
                .anyMatch(r -> r == Role.ROLE_ADMIN || r == Role.ROLE_AGENT);
        if (containsForbidden) {
            throw new ValidationException(ErrorMessages.ROLE_PUBLIC_INTERDIT);
        }
        return roles;
    }
}
