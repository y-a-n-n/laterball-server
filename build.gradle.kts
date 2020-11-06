val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project
val koin_version: String by project

plugins {
    application
    war
    kotlin("jvm") version "1.4.10"
    id("com.google.cloud.tools.appengine") version "2.2.0"
}

group = "com.laterball.server"
version = "2.1.0"

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
    implementation("io.ktor:ktor-client-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-gson:$ktor_version")
    implementation("io.ktor:ktor-client-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-servlet:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-gson:$ktor_version")
    implementation("io.ktor:ktor-locations:$ktor_version")
    implementation("io.ktor:ktor-metrics:$ktor_version")
    implementation("org.koin:koin-ktor:$koin_version")
    implementation("org.koin:koin-logger-slf4j:$koin_version")
    implementation("org.koin:koin-test:$koin_version")
    implementation("io.ktor:ktor-html-builder:$ktor_version")
    implementation("com.google.cloud:google-cloud-logging-logback:0.118.3-alpha")
    implementation("com.google.cloud:google-cloud-datastore:1.105.0")
    implementation("org.twitter4j", "twitter4j-core", "4.0.7")

    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktor_version")
    testImplementation("ch.qos.logback:logback-classic:$logback_version")
}

appengine {
    deploy {
        projectId = "laterball"
        version = "1"
        stopPreviousVersion = true
        promote = true
    }
}

extra.apply{
    set("gce_logback_version", "0.60.0-alpha")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

