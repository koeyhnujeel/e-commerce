import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
	kotlin("jvm") version "1.9.25" apply false
	kotlin("plugin.allopen") version "1.9.25" apply false
	kotlin("plugin.spring") version "1.9.25" apply false
	kotlin("plugin.jpa") version "1.9.25" apply false
	id("org.springframework.boot") version "3.5.11" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
}

group = "zoonza"
version = "0.0.1-SNAPSHOT"

allprojects {
	group = "zoonza"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}
}

subprojects {
	pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
		extensions.configure<KotlinJvmProjectExtension> {
			jvmToolchain(21)
			compilerOptions {
				freeCompilerArgs.addAll("-Xjsr305=strict")
			}
		}

		dependencies {
			"testImplementation"("org.jetbrains.kotlin:kotlin-test-junit5")
			"testImplementation"("io.kotest:kotest-assertions-core-jvm:5.9.1")
			"testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
		}
	}

	tasks.withType<Test>().configureEach {
		useJUnitPlatform()
	}
}