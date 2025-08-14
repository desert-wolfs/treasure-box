plugins {
    java
    id("org.springframework.boot") version "3.5.3"   //java 21
    id("io.spring.dependency-management") version "1.1.7" //java 21
//    id("org.springframework.boot") version "3.2.10"
//    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.douniu"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
//        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // MyBatis 核心依赖
    implementation("org.mybatis:mybatis:3.5.11")
    // MyBatis Generator 依赖
    implementation("org.mybatis.generator:mybatis-generator-core:1.4.1")
    // 数据库驱动，这里以 MySQL 为例，根据实际情况替换
    implementation("mysql:mysql-connector-java:8.0.28")
    implementation("cn.hutool:hutool-all:5.8.24")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    // fastjson
    implementation("com.alibaba:fastjson:2.0.33")
    // gson
    implementation("com.google.code.gson:gson:2.10.1")
    // google.common
    implementation("com.google.guava:guava:31.1-jre")
    // commons.lang3
    implementation("org.apache.commons:commons-lang3:3.12.0")
    // commons.collections
    implementation("org.apache.commons:commons-collections4:4.4")
    // commons.io
    implementation("commons-io:commons-io:2.11.0")
}



tasks.withType<Test> {
    useJUnitPlatform()
}