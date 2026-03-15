package com.example.gestionimmobilier.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Constraint(validatedBy = BusinessEmailValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface BusinessEmail {

    String message() default "Un email professionnel (entreprise) est requis. Les adresses personnelles ne sont pas acceptées.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
