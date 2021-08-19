# Android In-App Billing Library v4+ [![API](https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=16) [![JitCI](https://jitci.com/gh/moisoni97/google-inapp-billing/svg)](https://jitci.com/gh/moisoni97/google-inapp-billing) [![JitPack](https://jitpack.io/v/moisoni97/google-inapp-billing.svg)](https://jitpack.io/#moisoni97/google-inapp-billing)
A simple implementation of the Android In-App Billing API.

### Demo Application for In-App Billing [https://play.google.com/store/apps/details?id=com.shiv.shambhu](https://play.google.com/store/apps/details?id=com.shiv.shambhu)

### Made Possible by
###### [https://github.com/moisoni97/google-inapp-billing](https://github.com/moisoni97/google-inapp-billing)

# Getting Started

* You project should build against Android 4.1.x (minSdkVersion 16).

* Add the JitPack repository to your project's build.gradle file:

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

* Add the dependency in your app's build.gradle file:

```gradle
dependencies {
    implementation 'com.github.moisoni97:google-inapp-billing:1.0.5'
}
```

* Open the AndroidManifest.xml of your application and add this permission:

```xml
  <uses-permission android:name="com.android.vending.BILLING" />
```

# Important Notice

* For builds that use `minSdkVersion` lower than `24` it is very important to include the following in your app's build.gradle file:

```gradle
android {
  compileOptions {
    coreLibraryDesugaringEnabled true
    
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
}

dependencies {
  coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:1.1.5'
}
```

* For builds that use `minSdkVersion` lower than `21` add the above and also this:

```gradle
android {
    defaultConfig {
        multiDexEnabled true
    }
}
```

This step is required to enable support for some APIs on lower SDK versions that aren't available natively only starting from `minSdkVersion 24`.
