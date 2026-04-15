🔒 Vocal-Lock: Voice-Activated AppLocker
========================================

> **Your Voice · Your Keys · Your Device.**

Vocal-Lock is a privacy-first, offline-only Android application security vault. Instead of relying purely on easily compromised PINs or patterns, Vocal-Lock leverages on-device voice recognition and advanced Android system services to secure your sensitive applications.

Engineered with bleeding-edge Android architecture (Target SDK 36, Jetpack Compose, Kotlin 2.x, Koin), Vocal-Lock ensures that your apps are locked down instantly when they are closed or swiped away, and only open when they hear your specific secret phrase. This project demonstrates enterprise-grade architectural patterns, advanced UI/UX state management, and deep integration with strict Android OS limitations.

🛡 The Privacy & Security Promise
---------------------------------

Privacy isn't just a feature here; it is the absolute foundation of the codebase.

*   **No Internet Permission:** The tag does not exist in this manifest.
    
*   **No Cloud Backups:** Auto-backup is explicitly disabled (android:allowBackup="false").
    
*   **Zero Telemetry:** No analytics, no tracking, no Crashlytics.
    
*   **On-Device Processing:** Speech recognition is handled entirely by the local Android SpeechRecognizer API. Your voice data never leaves your local hardware.
    
*   **Encrypted Storage:** Secrets and passphrases are obfuscated and stored locally via Room (androidx.room) and Proto DataStore.
    

✨ Core Features
---------------

*   **🎙 Voice-Key Unlocking:** Speak a custom phrase to unlock individual apps or entire groups. The lock screen features a custom native Compose foreground microphone implementation that prevents early cut-offs and UI freezing.
    
*   **🗂 Bento-Grid Vault (Physics UI):** Organize your locked apps into groups using an advanced, fully custom **Drag-and-Drop** physics UI built natively in Compose. Apps dragged into a group automatically inherit the group's master authentication settings.
    
*   **👁 Accessibility Watchdog:** A highly optimized background service (VocalLockAccessibilityService) that tracks AccessibilityEvent.TYPE\_WINDOW\_STATE\_CHANGED to instantly detect and block access to protected apps.
    
*   **🛡 Process Death Trap:** Leverages UsageStatsManager to detect when a locked app is killed from the Recent Apps menu (Event 24), wiping session memory and instantly locking it down for the next launch.
    
*   **🔔 Smart Security Nudges:** Advisory local notifications (NotificationManager) remind users to set up voice prints if they rely too heavily on fallback text passwords during a single session.
    

🛠 Technical Architecture & Stack
---------------------------------

Vocal-Lock is built strictly adhering to **Clean Architecture** and **MVVM** principles, ensuring extreme separation of concerns and high testability.

### The Stack

*   **UI Layer:** Jetpack Compose (with Kotlin 2.x Compose Compiler plugin)
    
*   **Architecture:** MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF)
    
*   **Dependency Injection:** Koin with KSP (Kotlin Symbol Processing) for compile-time module generation, completely avoiding reflection overhead.
    
*   **Local Persistence:** Room Database (Offline SQLite) for relational app/group data, and Proto DataStore for type-safe global settings and persistent state.
    
*   **Asynchrony:** Kotlin Coroutines & Kotlin Flows (StateFlow, SharedFlow)
    
*   **Build System:** Gradle Kotlin DSL (AGP 9.1.0+)
    

### Module Structure

*   com.vocallock.core: Foundational UI components, theme definitions (custom Squircle shapes, VLColor palettes), navigation graphs, and permission managers.
    
*   com.vocallock.data: Room DAOs, Entities (AppEntity, GroupEntity), Repository implementations, and Proto DataStore serializers.
    
*   com.vocallock.domain: Pure Kotlin UseCases containing the core business logic (e.g., VerifyVoicePhraseUseCase, ShouldShowNudgeUseCase) and abstract Repository interfaces.
    
*   com.vocallock.feature: Self-contained UI features (home, detail, selector, overlay, settings) containing their respective Screens, Components, and ViewModels.
    
*   com.vocallock.service: Android System Services, BroadcastReceivers, and Notification management representing the "Watchdog" layer.
    

🔐 System Permissions Breakdown
-------------------------------

To build a bulletproof AppLocker, Vocal-Lock requires deep, explicit system access. The app includes a rigorous "6-Pillar Setup Wall" to guide users through granting these securely:

1.  **Microphone (RECORD\_AUDIO):** Required for the foreground lock screen to listen for your secret voice phrase.
    
2.  **Display Over Apps (SYSTEM\_ALERT\_WINDOW):** Required to launch the LockActivity seamlessly over top of protected apps using Intent.FLAG\_ACTIVITY\_NEW\_TASK.
    
3.  **Usage Access (PACKAGE\_USAGE\_STATS):** Required to query the UsageStatsManager and detect foreground app launches and process deaths in real-time.
    
4.  **Accessibility Service (BIND\_ACCESSIBILITY\_SERVICE):** Required to monitor window state changes incredibly efficiently without draining the battery via polling.
    
5.  **Ignore Battery Optimizations:** Required to prevent the Android OS from killing the Watchdog service while the device is dozing.
    
6.  **Notifications (POST\_NOTIFICATIONS):** Required to deliver local security nudges.
    

🚀 Getting Started (Local Development)
--------------------------------------

### Prerequisites

*   Android Studio (Ladybug or newer recommended)
    
*   JDK 17
    
*   Android Emulator or Physical Device running Android 13 (API 33) or higher.
    

### Installation

1.  Clone the repository
    ```bash
    git clone https://github.com/RaxitGamdha-R52G/Vocal-Lock.git
    ```
    
2.  Open the project in Android Studio.
    
3.  Sync Gradle.
    
4.  Build and run the app. *(Note: The local build relies on environment variables for versioning. If none are provided by a CI/CD runner, it will default to 1.0.0-dev via the build.gradle.kts configuration).*
    

🛣 Roadmap
----------

**v0.1.0 (Current Baseline)**

*   \[x\] Core Accessibility Watchdog & UsageStats monitor
    
*   \[x\] Native Compose Speech Recognizer overlay
    
*   \[x\] Advanced Drag-and-drop Group Management
    
*   \[x\] Proto DataStore Settings & System Nudges

🤝 Contributing & CI/CD
-----------------------

Vocal-Lock uses an automated Semantic Versioning (SemVer) CI/CD pipeline. All commits must adhere strictly to the **Conventional Commits** specification (`feat:`, `fix:`, `chore:`, etc.). 

To enforce this locally, we version-control our Git hooks. After cloning the repository, you must run this single command to activate the local commit linter:

```bash
git config core.hooksPath .githooks
```
