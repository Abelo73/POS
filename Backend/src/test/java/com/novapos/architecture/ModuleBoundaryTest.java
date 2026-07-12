package com.novapos.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class ModuleBoundaryTest {

    private static final String BASE_PACKAGE = "com.novapos";

    private static final Set<String> MODULES = Set.of(
            "company", "user", "catalog", "inventory", "pos",
            "purchasing", "customer", "promotion", "accounting",
            "restaurant", "reporting", "notification"
    );

    private static JavaClasses importedClasses;

    @BeforeAll
    static void importClasses() {
        importedClasses = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(BASE_PACKAGE);
    }

    @Test
    @DisplayName("No class in any module's domain package may be accessed from outside that module")
    void domainClassesMustNotBeAccessedFromOutsideModule() {
        for (String module : MODULES) {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE_PACKAGE + "." + module + ".domain..")
                    .should().onlyBeAccessed()
                    .byAnyPackage(BASE_PACKAGE + "." + module + "..")
                    .allowEmptyShould(true);
            rule.check(importedClasses);
        }
    }

    @Test
    @DisplayName("No class in any module's repository package may be accessed from outside that module")
    void repositoryClassesMustNotBeAccessedFromOutsideModule() {
        for (String module : MODULES) {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE_PACKAGE + "." + module + ".repository..")
                    .should().onlyBeAccessed()
                    .byAnyPackage(BASE_PACKAGE + "." + module + "..")
                    .allowEmptyShould(true);
            rule.check(importedClasses);
        }
    }

    @Test
    @DisplayName("No class in any module's service package may be accessed from outside that module (except by its own api package)")
    void serviceClassesMustNotBeAccessedFromOutsideModule() {
        for (String module : MODULES) {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE_PACKAGE + "." + module + ".service..")
                    .should().onlyBeAccessed()
                    .byAnyPackage(BASE_PACKAGE + "." + module + "..")
                    .allowEmptyShould(true);
            rule.check(importedClasses);
        }
    }

    @Test
    @DisplayName("Only module.api packages may be imported by other modules")
    void onlyApiPackagesMayBeImportedAcrossModules() {
        for (String module : MODULES) {
            DescribedPredicate<JavaClass> allowedTarget = JavaClass.Predicates
                    .resideOutsideOfPackage(BASE_PACKAGE + "..")
                    .or(JavaClass.Predicates.resideInAPackage(BASE_PACKAGE + ".shared.."))
                    .or(JavaClass.Predicates.resideInAPackage(BASE_PACKAGE + "." + module + ".."))
                    .or(JavaClass.Predicates.resideInAnyPackage(
                            MODULES.stream()
                                    .filter(m -> !m.equals(module))
                                    .map(m -> BASE_PACKAGE + "." + m + ".api..")
                                    .toArray(String[]::new)));

            ArchRule rule = classes()
                    .that().resideInAPackage(BASE_PACKAGE + "." + module + "..")
                    .and().resideOutsideOfPackage(BASE_PACKAGE + "." + module + ".api..")
                    .and().resideOutsideOfPackage(BASE_PACKAGE + ".shared..")
                    .should().onlyAccessClassesThat(allowedTarget)
                    .allowEmptyShould(true);
            rule.check(importedClasses);
        }
    }
}
