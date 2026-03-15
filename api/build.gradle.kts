plugins {
	kotlin("jvm")
	kotlin("plugin.jpa")
	kotlin("plugin.spring")
	id("org.springframework.boot")
	id("io.spring.dependency-management")
}

dependencies {
	implementation(project(":application"))
	implementation(project(":domain"))

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation(kotlin("reflect"))

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("io.jsonwebtoken:jjwt-api:0.12.7")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.7")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.7")

	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	runtimeOnly("com.mysql:mysql-connector-j")

	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mysql")
}
