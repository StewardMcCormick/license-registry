plugins {
    id("java")
    id("war")
}

group = "com.mccormick"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

dependencies {
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    compileOnly("jakarta.servlet.jsp:jakarta.servlet.jsp-api:3.1.1")
    implementation("jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api:3.0.0")
    implementation("org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1")
    implementation("org.postgresql:postgresql:42.7.14")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    exclude("**/.env")
}

tasks.war {
    exclude("**/.env")
}

tasks.test {
    useJUnitPlatform()
}
