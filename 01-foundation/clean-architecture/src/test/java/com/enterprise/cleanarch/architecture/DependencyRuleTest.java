package com.enterprise.cleanarch.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces the Clean Architecture dependency rule: inner layers must not depend on
 * outer layers or on frameworks. This makes the architecture verifiable, not just
 * a convention.
 */
@AnalyzeClasses(packages = "com.enterprise.cleanarch", importOptions = ImportOption.DoNotIncludeTests.class)
class DependencyRuleTest {

    @ArchTest
    static final ArchRule domain_is_free_of_outer_layers_and_frameworks = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application..", "..adapter..", "..config..",
                    "org.springframework..", "jakarta.persistence..");

    @ArchTest
    static final ArchRule application_is_free_of_adapters_and_frameworks = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..adapter..", "..config..",
                    "org.springframework..", "jakarta.persistence..");
}
