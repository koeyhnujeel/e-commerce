package zoonza.commerce

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

class ModulithArchitectureTests {
    @Test
    fun `verify module structure`() {
        ApplicationModules.of(CommerceApplication::class.java).verify()
    }

    @Test
    fun `module list`() {
        val modules = ApplicationModules.of(CommerceApplication::class.java)

        for (module in modules) {
            println(module.toString())
        }
    }
}
