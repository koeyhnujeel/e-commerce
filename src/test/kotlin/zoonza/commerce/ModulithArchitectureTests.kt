package zoonza.commerce

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

class ModulithArchitectureTests {
    @Test
    fun `verify module structure`() {
        ApplicationModules.of(CommerceApplication::class.java).verify()
    }

    @Test
    fun `catalog like inventory modules are part of the modulith`() {
        val modules = ApplicationModules.of(CommerceApplication::class.java)
        val catalogModule = modules.getModuleByName("catalog")
        val inventoryModule = modules.getModuleByName("inventory")
        val likeModule = modules.getModuleByName("like")

        assertThat(catalogModule).isPresent
        assertThat(inventoryModule).isPresent
        assertThat(likeModule).isPresent
    }
}
