plugins {
    id 'com.android.library'
}

android {
    namespace 'bin.mt.signature'
    compileSdk 32
    ndkVersion '24.0.8215888'

    defaultConfig {
        minSdk 21
        //noinspection OldTargetApi
        targetSdk 32
        externalNativeBuild {
            cmake {
                cppFlags ""
                abiFilters = ["armeabi-v7a", "x86", "arm64-v8a", "x86_64"]
            }
        }
        ndk {
            abiFilters = ["armeabi-v7a", "x86", "arm64-v8a", "x86_64"]
        }
    }

    externalNativeBuild {
        cmake {
            path "CMakeLists.txt"
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'org.lsposed.hiddenapibypass:hiddenapibypass:4.3'
}