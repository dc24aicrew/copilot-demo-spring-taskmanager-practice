package com.demo.copilot.taskmanager.infrastructure.validation;

import com.demo.copilot.taskmanager.infrastructure.security.InputSanitizer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Validator for secure input that checks for XSS and injection attacks.
 */
public class SecureInputValidator implements ConstraintValidator<SecureInput, String> {

    @Autowired
    private InputSanitizer inputSanitizer;

    @Override
    public void initialize(SecureInput constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // Let @NotBlank handle empty values
        }

        if (inputSanitizer == null) {
            // Fallback validation if sanitizer is not available
            return !value.contains("<script") && !value.contains("javascript:");
        }

        return !inputSanitizer.isDangerous(value);
    }
}