package com.demo.copilot.taskmanager.user.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Architecture tests to ensure Clean Architecture compliance.
 * 
 * These tests validate that the application follows proper layering
 * and dependency direction rules according to Clean Architecture principles.
 */
@AnalyzeClasses(packages = "com.demo.copilot.taskmanager.user")
public class CleanArchitectureTest {

    /**
     * Test that validates the overall layered architecture.
     * Ensures that dependencies flow in the correct direction.
     */
    @ArchTest
    static final ArchRule layered_architecture_should_be_respected =
        layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .layer("Presentation").definedBy("..presentation..")
            
            .whereLayer("Domain").mayNotAccessAnyLayer()
            .whereLayer("Application").mayOnlyAccessLayers("Domain")
            .whereLayer("Infrastructure").mayOnlyAccessLayers("Domain", "Application")
            .whereLayer("Presentation").mayOnlyAccessLayers("Domain", "Application");

    /**
     * Domain layer should not depend on any infrastructure concerns.
     */
    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..infrastructure..",
                "org.springframework..",
                "jakarta.persistence..",
                "javax.persistence.."
            );

    /**
     * Domain layer should not depend on application layer.
     */
    @ArchTest
    static final ArchRule domain_should_not_depend_on_application =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..application..");

    /**
     * Domain layer should not depend on presentation layer.
     */
    @ArchTest
    static final ArchRule domain_should_not_depend_on_presentation =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..presentation..");

    /**
     * Application layer should not depend on infrastructure.
     */
    @ArchTest
    static final ArchRule application_should_not_depend_on_infrastructure =
        noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    /**
     * Application layer should not depend on presentation.
     */
    @ArchTest
    static final ArchRule application_should_not_depend_on_presentation =
        noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..presentation..");

    /**
     * Domain entities should not have JPA annotations.
     */
    @ArchTest
    static final ArchRule domain_entities_should_not_have_jpa_annotations =
        noClasses().that().resideInAPackage("..domain.entity..")
            .should().beAnnotatedWith("jakarta.persistence.Entity")
            .orShould().beAnnotatedWith("javax.persistence.Entity");

    /**
     * Domain value objects should not have JPA annotations.
     */
    @ArchTest
    static final ArchRule domain_value_objects_should_not_have_jpa_annotations =
        noClasses().that().resideInAPackage("..domain.valueobject..")
            .should().beAnnotatedWith("jakarta.persistence.Embeddable")
            .orShould().beAnnotatedWith("javax.persistence.Embeddable");

    /**
     * Repository implementations should be in infrastructure layer.
     */
    @ArchTest
    static final ArchRule repository_implementations_should_be_in_infrastructure =
        classes().that().haveNameMatching(".*RepositoryImpl")
            .should().resideInAPackage("..infrastructure..");

    /**
     * Repository interfaces should be in domain layer.
     */
    @ArchTest
    static final ArchRule repository_interfaces_should_be_in_domain =
        classes().that().areInterfaces()
            .and().haveNameMatching(".*Repository")
            .and().areNotAnnotatedWith("org.springframework.data.repository.Repository")
            .should().resideInAPackage("..domain.repository..");

    /**
     * Use cases should be in application layer.
     */
    @ArchTest
    static final ArchRule use_cases_should_be_in_application_layer =
        classes().that().haveNameMatching(".*UseCase")
            .should().resideInAPackage("..application.usecase..");

    /**
     * Controllers should be in presentation layer.
     */
    @ArchTest
    static final ArchRule controllers_should_be_in_presentation_layer =
        classes().that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .should().resideInAPackage("..presentation..");

    /**
     * Domain services should only use domain objects.
     */
    @ArchTest
    static final ArchRule domain_services_should_only_use_domain_objects =
        classes().that().resideInAPackage("..domain.service..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                "..domain..",
                "java..",
                "org.slf4j.."
            );

    /**
     * JPA entities should only be in infrastructure layer.
     */
    @ArchTest
    static final ArchRule jpa_entities_should_be_in_infrastructure =
        classes().that().areAnnotatedWith("jakarta.persistence.Entity")
            .should().resideInAPackage("..infrastructure..");
}