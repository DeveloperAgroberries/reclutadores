plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.kotlin.parcelize)
    //alias(libs.plugins.kotlin.ksp) // <--- ¡AÑADE ESTA LÍNEA!

}

android {
    namespace = "com.agroberriesmx.reclutadores"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.agroberriesmx.reclutadores"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            resValue("string", "AgroberriesMX", "[DEBUG]Reclutadores Agroberries MX")
            //buildConfigField("String", "BASE_URL", "\"http://54.165.41.23:5053/api/ReclutadoresApp/\"")
            buildConfigField("String", "BASE_URL", "\"http://192.168.1.43:5011/api/ReclutadoresApp/\"")

        }

        release {
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            resValue("string", "AgroberriesMX", "[DEBUG]Reclutadores Agroberries MX")
            //buildConfigField("String", "BASE_URL", "\"http://54.165.41.23:5053/api/ReclutadoresApp/\"")
            buildConfigField("String", "BASE_URL", "\"http://192.168.1.43:5011/api/ReclutadoresApp/\"")
        }
    }
    testBuildType = "debug" // 👈 añade esto aquí
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true // <--- Añade esta línea
        viewBinding = true
    }
}

dependencies {

    val versionCameraX = "1.5.3"
    val mlKit = "16.0.1"
    val materialVersion = "1.13.0"

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //NavComponent
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    //Dagger Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    //ksp(libs.hilt.compiler) // <--- CAMBIA 'kapt' a 'ksp'

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    //Okhttp
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    //Sqlite
    implementation(libs.androidx.sqlite)

    //Glide
    implementation(libs.glide)
    implementation(libs.glide.annotations)
    kapt(libs.glide.compiler)
    //ksp(libs.glide.compiler) // <--- CAMBIA 'kapt' a 'ksp'

    //ZXing
    implementation(libs.zxing)

    // ML Kit Text Recognition
    implementation("com.google.mlkit:text-recognition:$mlKit")

    // CameraX core library
    implementation("androidx.camera:camera-camera2:$versionCameraX")
    implementation("androidx.camera:camera-lifecycle:$versionCameraX")
    implementation("androidx.camera:camera-view:$versionCameraX")
    implementation("androidx.camera:camera-extensions:$versionCameraX")

    implementation ("com.google.android.material:material:$materialVersion") // O la versión más reciente

    // Estas son las que habilitan "by viewModels()"
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.activity:activity-ktx:1.9.3")

}