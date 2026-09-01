# Shopirend

Shopirend is a mobile-first social shopping MVP. A shopper starts a store trip, chooses friends to notify, receives item requests, marks them bought or not found, and closes the trip. Private shopping lists and explicitly shared wishlists support that main flow.

The repository is an Android Studio project with two modules:

- `android-app`: Kotlin, Jetpack Compose, Material 3, ViewModel, Navigation Compose, Ktor Client, kotlinx.serialization, Hilt, DataStore, and Firebase Messaging.
- `backend`: Kotlin, Spring Boot, Spring Security/JWT, Spring Data JPA, PostgreSQL, Flyway, and an abstracted notification service with logging and Firebase implementations.

## What is implemented

- Registration, login, JWT sessions, profile updates, and BCrypt password hashing
- Friend requests, acceptance, removal, and accepted-friend privacy checks
- Predefined stores and fast trip creation with selected recipients
- Active-trip visibility, live five-second refresh, item requests, bought/not-found updates, cancellation, and completion
- Private personal shopping lists, with one-tap addition to an active trip
- Wishlists shared only with selected accepted friends
- Device token registration and optional Firebase Cloud Messaging
- PostgreSQL schema managed by Flyway

## Run it in Android Studio

### 1. Open and sync

1. Start Android Studio.
2. Choose **Open** and select `C:\Git\Shopirend`.
3. If Android Studio asks whether to trust the project, choose **Trust Project**.
4. In **Settings > Build, Execution, Deployment > Build Tools > Gradle**, choose **Gradle JDK: Embedded JDK**.
5. Let Gradle sync finish. If Android Studio asks to install Android SDK 37 or Build Tools 36.0.0, accept.

Android Studio normally creates `local.properties` automatically. The checked-in `local.properties.example` shows the required format. The default API URL is already `http://10.0.2.2:8080/api/`, which is how an Android emulator reaches your computer.

### 2. Start PostgreSQL

The simplest option is Docker Desktop. Open Android Studio's **Terminal** tab in the repository root and run:

```powershell
docker compose up -d
```

This starts PostgreSQL on port `5432` with database, username, and password all set to `shopirend`. The data is kept in a Docker volume.

If you already have PostgreSQL, create a database and user yourself and supply their values to the backend run configuration in the next step.

### 3. Run the Spring Boot backend

1. Open `backend/src/main/kotlin/com/shopirend/ShopirendApplication.kt`.
2. Click the green run arrow beside `main` and choose **Modify Run Configuration**.
3. Add these environment variables:

```text
DB_URL=jdbc:postgresql://localhost:5432/shopirend;DB_USERNAME=shopirend;DB_PASSWORD=shopirend;JWT_SECRET=replace-this-with-a-long-random-development-secret;FCM_ENABLED=false
```

4. Run `ShopirendApplicationKt`.
5. Wait for `Started ShopirendApplication` in the Run window.

Flyway creates the schema and inserts Lidl, Kaufland, Tesco, Albert, Billa, DM, and Other on first startup. To verify the backend manually, open `http://localhost:8080/api/actuator/health`; it should return `{"status":"UP"}`.

You can also run the backend from Android Studio's Gradle tool window: **Shopirend > backend > Tasks > application > bootRun**. Environment variables still need to be available to that process.

### 4. Create an emulator and run the app

1. Open **Tools > Device Manager**.
2. Create a Pixel virtual device with an API 37 system image.
3. Start the emulator.
4. Select the `android-app` run configuration and the emulator in the toolbar.
5. Click **Run**.

If you use a physical phone, copy `local.properties.example` to `local.properties`, set `SHOPIREND_API_BASE_URL` to your computer's LAN IP, and allow inbound TCP port `8080` in Windows Firewall. The phone and computer must be on the same network.

## Walk through the MVP with two users

Use two emulators, or one emulator plus a physical phone. A single emulator also works if you sign out and switch accounts, but two devices make the updates easier to observe.

1. On device A, register Onur with a unique email.
2. On device B, register Mehmet with another email.
3. Onur opens **Friends**, taps the add-person button, and enters Mehmet's email.
4. Mehmet opens **Friends** and accepts the request.
5. Onur taps **Start a shopping trip**, selects **Lidl**, selects Mehmet, and starts.
6. Mehmet refreshes Home and opens Onur's Lidl trip.
7. Mehmet requests `2× Cola Zero`.
8. Onur's open trip refreshes automatically within five seconds. Onur taps **Bought**.
9. Mehmet's trip view refreshes and shows **bought**.
10. Onur taps **Finish trip**.

The refresh button is available on Home and trip details when you want an immediate update. Push notifications are intentionally optional for this first API-driven walkthrough.

## Enable Firebase Cloud Messaging later

1. Create a Firebase project and add an Android app with package name `com.shopirend.android`.
2. Download `google-services.json` to `android-app/google-services.json`. The build detects the file and enables the Google Services plugin automatically.
3. In Firebase/Google Cloud, create or select a service account that can send FCM messages and download its JSON key outside this repository.
4. Change the backend run configuration to include:

```text
FCM_ENABLED=true;GOOGLE_APPLICATION_CREDENTIALS=C:\absolute\path\to\service-account.json
```

5. Sync Gradle, reinstall/run the app, sign in, and accept the Android notification permission.
6. Restart the backend.

The app registers its current FCM token after authentication. Notifications are sent when a selected friend starts a trip, when a friend requests an item from the shopper, and when the shopper changes that item's status. Credential and Google Services files are excluded by `.gitignore`.

## Useful configuration

| Setting | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/shopirend` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `shopirend` | PostgreSQL user |
| `DB_PASSWORD` | `shopirend` | PostgreSQL password |
| `JWT_SECRET` | development fallback | JWT signing secret; always override outside local development |
| `FCM_ENABLED` | `false` | Switch between logging and Firebase notifications |
| `SHOPIREND_API_BASE_URL` | `http://10.0.2.2:8080/api/` | Android client's backend URL in `local.properties` |

## Project map

```text
android-app/src/main/kotlin/com/shopirend/android/
  data/           API DTOs, Ktor client, repository, persisted JWT session
  notification/   FCM service and token registration
  ui/             Compose screens, navigation, and AppViewModel

backend/src/main/kotlin/com/shopirend/
  api/            Controllers, request/response DTOs, error handling
  model/          JPA entities and enums
  repository/     Spring Data repositories
  security/       JWT filter and Spring Security configuration
  service/        Business flows and notification abstraction

backend/src/main/resources/db/migration/
  V1__initial_schema.sql
```

The schema keeps future retailer products, offers, and simple debts separate from the MVP concepts. No payments, wallet, retailer catalog, GPS, chat, price comparison, or recommendation features are included.

