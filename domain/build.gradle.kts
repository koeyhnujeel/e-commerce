plugins {
	id("java-library")
	kotlin("jvm")
	kotlin("plugin.allopen")
	kotlin("plugin.jpa")
}

dependencies {
	implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}
