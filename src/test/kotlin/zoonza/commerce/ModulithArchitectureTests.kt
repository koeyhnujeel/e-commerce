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
    fun `payment module is part of the modulith`() {
        val modules = ApplicationModules.of(CommerceApplication::class.java)
        val paymentModule = modules.getModuleByName("payment")
        val orderModule = modules.getModuleByName("order")

        assertThat(paymentModule).isPresent
        assertThat(orderModule).isPresent
    }
}
