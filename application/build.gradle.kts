plugins {
	id("java-library")
	kotlin("jvm")
	kotlin("plugin.spring")
	id("io.spring.dependency-management")
}

dependencies {
	implementation(project(":domain"))
	implementation(kotlin("reflect"))

	implementation("org.springframework:spring-context")
	implementation("org.springframework:spring-tx")

	testImplementation("io.mockk:mockk:1.14.2")
}
