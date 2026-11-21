import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "com.dsy1105.therionlabsapp"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.dsy1105.therionlabsapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
        getByName("debug") {
            enableUnitTestCoverage = true
        }

        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.room.runtime)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.14.6")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

}

jacoco {
    toolVersion = "0.8.14"
}

val clasesDeLogica = listOf(
    "**/com/dsy1105/therionlabsapp/interfaz/UsuarioViewModel*.class",
    "**/com/dsy1105/therionlabsapp/interfaz/ProspecctoViewModel*.class",
    "**/com/dsy1105/therionlabsapp/interfaz/Validaciones*.class",
    "**/com/dsy1105/therionlabsapp/interfaz/ReglasPostulacion*.class",
    "**/com/dsy1105/therionlabsapp/datos/UsuarioRepositorio*.class",
    "**/com/dsy1105/therionlabsapp/datos/ProspectoRepositorio*.class",
    "**/com/dsy1105/therionlabsapp/datos/SeguridadClave*.class",
    "**/com/dsy1105/therionlabsapp/datos/AccesoAdmin*.class",
    "**/com/dsy1105/therionlabsapp/datos/FuncionesDocumento*.class"
)

val clasesCompiladas = layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")

val archivosClasesLogica = fileTree(clasesCompiladas) {
    clasesDeLogica.forEach { include(it.removePrefix("**/")) }
}

val archivoEjecucionJacoco = layout.buildDirectory.file(
    "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
)

tasks.register<JacocoReport>("reporteCoberturaLogica") {
    dependsOn("testDebugUnitTest")
    classDirectories.setFrom(archivosClasesLogica)
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(files(archivoEjecucionJacoco))
    reports {
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/logica"))
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/logica/reporte.xml"))
    }
}

tasks.register<JacocoCoverageVerification>("verificar80Logica") {
    dependsOn("reporteCoberturaLogica")
    classDirectories.setFrom(archivosClasesLogica)
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(files(archivoEjecucionJacoco))
    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
