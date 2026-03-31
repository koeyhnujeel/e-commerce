package zoonza.commerce

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class LayerDependencyArchitectureTests {
    private val threeLayerModules = listOf(
        "auth",
        "cart",
        "catalog",
        "inventory",
        "like",
        "member",
        "order",
        "verification",
    )

    @TestFactory
    fun `verify module layer dependency direction`(): List<DynamicTest> {
        return threeLayerModules.flatMap(::moduleLayerRules)
    }

    private fun moduleLayerRules(moduleName: String): List<DynamicTest> {
        val basePackage = "zoonza.commerce.$moduleName"
        val importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(basePackage)

        return listOf(
            dynamicTest("[$moduleName] application does not depend on adapter") {
                noClasses()
                    .that().resideInAnyPackage("$basePackage.application..")
                    .should().dependOnClassesThat().resideInAnyPackage("$basePackage.adapter..")
                    .check(importedClasses)
            },
            dynamicTest("[$moduleName] domain does not depend on application") {
                noClasses()
                    .that().resideInAnyPackage("$basePackage.domain..")
                    .should().dependOnClassesThat().resideInAnyPackage("$basePackage.application..")
                    .check(importedClasses)
            },
            dynamicTest("[$moduleName] domain does not depend on adapter") {
                noClasses()
                    .that().resideInAnyPackage("$basePackage.domain..")
                    .should().dependOnClassesThat().resideInAnyPackage("$basePackage.adapter..")
                    .check(importedClasses)
            },
        )
    }
}
