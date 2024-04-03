plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("kapt") version "1.9.23"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    kapt("org.openjdk.jcstress:jcstress-core:0.16")
    implementation("org.openjdk.jcstress:jcstress-core:0.16")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "org.openjdk.jcstress.Main"
    }

    from(
        configurations.runtimeClasspath.get().map(::zipTree)
    )
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}