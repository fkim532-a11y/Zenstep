# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Application

PedomètrePro — podomètre Android en Kotlin / Jetpack Compose.
- **applicationId** : `com.franckim.pedometrepro` · **namespace** : `com.france.pedometre`
- **minSdk** : 26 · **targetSdk** : 35 · **versionCode** : 1

## Commandes de build

```bash
# Depuis la racine du projet
./gradlew assembleDebug          # APK debug
./gradlew assembleRelease        # APK release (nécessite keystore.properties)
./gradlew installDebug           # Installer sur un appareil connecté
./gradlew test                   # Tests unitaires
./gradlew connectedAndroidTest   # Tests instrumentés (appareil requis)
```

Le build release est conditionnel : si `keystore.properties` est absent, la signature est ignorée mais la compilation réussit. Room utilise KAPT (pas KSP) — ne pas migrer sans tester.

## Architecture

### Flux de données principal

```
TYPE_STEP_COUNTER (capteur HW)
    → StepCounterService (foreground service)
        → StepRepository.updateSteps()  ← StateFlow lu par MainActivity
        → StepDao.insertOrUpdate()      ← Room (pedometre_db)
        → StepWidget.pushUpdate()       ← widget écran d'accueil
```

`StepRepository` est un `object` Kotlin (singleton JVM), pas un ViewModel. Le service écrit dedans depuis le thread principal ; l'UI le lit via `collectAsState()`. Le widget lit `StepRepository.currentSteps.value` de façon synchrone.

### Calcul du nombre de pas (invariant critique)

Le capteur `TYPE_STEP_COUNTER` fournit un total cumulé **depuis le dernier redémarrage du téléphone**. La formule dans `onSensorChangedInternal` :

```
finalStepsForToday = (totalStepsSincePhoneBoot - totalStepsAtStartOfToday) + stepsAlreadySavedBeforeRestart
```

- `totalStepsAtStartOfToday` : valeur du capteur au moment où le service a démarré pour la journée en cours (initialisé à -1, fixé à la première lecture).
- `stepsAlreadySavedBeforeRestart` : pas déjà enregistrés en DB avant un redémarrage de service en cours de journée (chargé depuis Room au démarrage).
- `.coerceAtLeast(0)` sur `currentSessionSteps` évite les valeurs négatives si le capteur se réinitialise.

### Reset de minuit

Quand `today != serviceCurrentDay` dans `onSensorChangedInternal`, tous les compteurs de session sont remis à zéro et `serviceCurrentDay` est mis à jour. La requête DB de démarrage s'exécute en coroutine IO puis repasse sur Main avec `withContext(Dispatchers.Main)` — elle vérifie `serviceCurrentDay == queryDay` avant d'appliquer son résultat pour ignorer les résultats obsolètes si minuit passe pendant la requête.

### Persistance

- **Room** (`pedometre_db`) : table `steps_table` — une ligne par date `yyyy-MM-dd`, colonne `steps Int`. Nettoyage automatique des entrées > 1 an au démarrage du service.
- **SharedPreferences** (`user_settings`) : objectif, taille, poids, sexe, foulée, date du dernier objectif atteint. `UserPrefs` est la classe d'accès ; `StepUtils` lit les mêmes prefs directement (sans passer par `UserPrefs`).

### Navigation

`MainActivity` gère un `NavHost` avec 4 routes :
- `home` → `ActivityScreen` (cercle de progression + 3 métriques)
- `history` → `HistoryScreen` (3 onglets : Calendrier / Semaines / Mois)
- `settings` → `SettingsScreen`
- `privacy` → `PrivacyPolicyScreen`

Le thème est entièrement dark (`Color(0xFF121212)` fond, `Color(0xFF00E676)` accent vert).

### Démarrage automatique

`BootReceiver` écoute `BOOT_COMPLETED`, `QUICKBOOT_POWERON` et `MY_PACKAGE_REPLACED` pour relancer le service après redémarrage ou mise à jour de l'app.

## TODO — Avant publication Play Store

### Bloquant
- [ ] **Data Safety** : remplir le formulaire Play Console (activité physique, pas de partage tiers, données locales uniquement)

### Déjà traité
- [x] Politique de confidentialité (écran in-app `PrivacyPolicyScreen` + lien depuis Réglages)
- [x] Signature release (`signingConfig` conditionnel via `keystore.properties`)
- [x] ProGuard (`isMinifyEnabled = true` en release)
- [x] `applicationId` renommé en `com.franckim.pedometrepro`
- [x] Capteur absent (message affiché si `sensorAvailable == false`)
- [x] Exemption batterie (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` demandé au démarrage)
- [x] Splash Screen (API `androidx.core:core-splashscreen` intégrée)
- [x] Widget écran d'accueil (`StepWidget`)
- [x] Notification enrichie (format : "X pas · Y% · Z km")
