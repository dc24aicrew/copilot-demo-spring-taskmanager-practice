package com.demo.copilot.taskmanager.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation for secure input that prevents XSS and injection attacks.
 */
@Documented
@Constraint(validatedBy = SecureInputValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecureInput {
    
    String message() default "Input contains potentially dangerous content";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}