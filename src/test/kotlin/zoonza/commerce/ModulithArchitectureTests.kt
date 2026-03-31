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
    fun `catalog like inventory cart order modules are part of the modulith`() {
        val modules = ApplicationModules.of(CommerceApplication::class.java)
        val cartModule = modules.getModuleByName("cart")
        val catalogModule = modules.getModuleByName("catalog")
        val inventoryModule = modules.getModuleByName("inventory")
        val likeModule = modules.getModuleByName("like")
        val orderModule = modules.getModuleByName("order")

        assertThat(cartModule).isPresent
        assertThat(catalogModule).isPresent
        assertThat(inventoryModule).isPresent
        assertThat(likeModule).isPresent
        assertThat(orderModule).isPresent
    }
}
