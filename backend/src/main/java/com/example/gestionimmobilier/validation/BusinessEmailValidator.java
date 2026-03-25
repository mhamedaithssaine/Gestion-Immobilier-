package com.example.gestionimmobilier.validation;

import com.example.gestionimmobilier.exception.ErrorMessages;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class BusinessEmailValidator implements ConstraintValidator<BusinessEmail, String> {

    private static final Set<String> DOMAINES_EMAIL_PERSONNELS = Set.of(
            "gmail.com", "googlemail.com", "yahoo.com", "yahoo.fr", "hotmail.com", "hotmail.fr",
            "outlook.com", "live.com", "live.fr", "msn.com", "free.fr", "orange.fr", "sfr.fr",
            "laposte.net", "wanadoo.fr", "voila.fr", "aol.com", "icloud.com", "me.com", "mac.com",
            "protonmail.com", "proton.me", "zoho.com", "yandex.com", "mail.com", "gmx.com", "gmx.fr"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; 
        }
        int at = value.indexOf('@');
        if (at <= 0 || at >= value.length() - 1) {
            return true; 
        }
        String domaine = value.substring(at + 1).trim().toLowerCase();
        if (DOMAINES_EMAIL_PERSONNELS.contains(domaine)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorMessages.AGENCE_EMAIL_PERSONNEL_NON_ACCEPTE)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
