plugins {
    java
}

sourceSets {
    create("tools") {
        java {
            srcDir("src/tools")
        }
    }
    main {
        java {
            srcDir("src/main")
        }
        resources {
            srcDir("src/assets")
        }
    }
}

tasks.named<JavaCompile>("compileToolsJava") {
    dependsOn(tasks.compileJava)
    classpath = sourceSets["main"].runtimeClasspath + sourceSets["main"].output
}

val genatlas = tasks.register<JavaExec>("genatlas") {
    mustRunAfter(tasks.classes)
    classpath = sourceSets["tools"].runtimeClasspath +
            sourceSets["main"].runtimeClasspath +
            sourceSets["main"].output

    workingDir = rootDir
    mainClass.set("core.tool.AtlasGenerator")
}


tasks.classes {
    finalizedBy(genatlas)
}

val lwjglVersion = "3.3.3"
val lwjglNatives = Pair(
    System.getProperty("os.name")!!,
    System.getProperty("os.arch")!!
).let { (name, arch) ->
    when {
        arrayOf("Linux", "FreeBSD", "SunOS", "Unit").any { name.startsWith(it) } ->
            if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
                "natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
            else
                "natives-linux"
        arrayOf("Windows").any { name.startsWith(it) }                           ->
            if (arch.contains("64"))
                "natives-windows${if (arch.startsWith("aarch64")) "-arm64" else ""}"
            else
                "natives-windows-x86"
        else -> throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
    }
}

allprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = "java")

    dependencies {
        implementation("com.google.code.gson:gson:2.10.1")
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
}

dependencies {
    implementation("com.gradleup.shadow:shadow-gradle-plugin:8.3.3")
    implementation("org.jcodec:jcodec:0.2.5")
    implementation("org.jcodec:jcodec-javase:0.2.5")

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-opengl")

    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
}
