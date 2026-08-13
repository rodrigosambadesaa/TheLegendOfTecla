package com.legendoftecla.architecture;

import com.legendoftecla.events.BusEventos;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/** Contratos ejecutables que impiden erosionar las fronteras arquitectonicas. */
class ArquitecturaTest {
    private static JavaClasses clases;

    @BeforeAll
    static void importarAplicacion() {
        clases = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.legendoftecla");
    }

    @Test
    void dominioYMotorNoDependenDeSwingNiDeLaGui() {
        noClasses().that().resideInAnyPackage(
                        "com.legendoftecla.model..",
                        "com.legendoftecla.engine..",
                        "com.legendoftecla.ai..",
                        "com.legendoftecla.effects..",
                        "com.legendoftecla.events..",
                        "com.legendoftecla.missions..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.legendoftecla.gui..", "javax.swing..")
                .check(clases);
    }

    @Test
    void dominioNoDependeDePersistenciaNiDeCargadores() {
        noClasses().that().resideInAnyPackage(
                        "com.legendoftecla.model..",
                        "com.legendoftecla.effects..",
                        "com.legendoftecla.events..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.legendoftecla.persistence..",
                        "com.legendoftecla.loader..")
                .check(clases);
    }

    @Test
    void busDeEventosNuncaEsUnGlobalEstatico() {
        noFields().that().haveRawType(BusEventos.class)
                .should().beStatic().check(clases);
    }
}
