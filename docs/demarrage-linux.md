# Développer le robot FTC sur Linux

Ce guide concerne le dépôt d'équipe
[Louis-Beaunoyer/FtcRobotController](https://github.com/Louis-Beaunoyer/FtcRobotController).
Le code du robot, la documentation et les scripts partagés restent dans ce dépôt.

## Préparer son ordinateur

- Installer Git et Android Studio pour Linux depuis leurs sources officielles.
- Utiliser Android Studio **Narwhal 3 Feature Drop ou ultérieur** pour FTC v12.0.
- Utiliser le JDK fourni avec Android Studio pour Gradle.
- Dans le gestionnaire SDK, installer **Android SDK Platform 30**, **Build-Tools
  35.0.0** et **Android SDK Platform-Tools**.
- Sur Ubuntu/Lubuntu, les règles USB du paquet `android-sdk-platform-tools-common`
  et l'appartenance au groupe `plugdev` permettent l'accès aux appareils Android.

Le projet fournit son propre Gradle : ne pas installer un Gradle système pour ce
projet et conserver les versions définies par FIRST. Un émulateur Android n'est
pas nécessaire pour programmer un Control Hub physique.

La configuration a été vérifiée sous Lubuntu 24.04 avec Android Studio Quail 4
2026.1.4 Patch 1 et son JDK 25.0.3. La base FTC v12.0 utilise Gradle 9.1.0 et
Android Gradle Plugin 8.13.2.

## Ouvrir le dépôt d'équipe

Après avoir accepté l'invitation GitHub de l'équipe, cloner ce dépôt :

```sh
git clone https://github.com/Louis-Beaunoyer/FtcRobotController.git
```

Dans Android Studio, ouvrir le dossier racine qui contient `settings.gradle`.
Laisser la synchronisation Gradle se terminer. Android Studio enregistre le chemin
du SDK dans `local.properties`, qui est propre à chaque ordinateur et ignoré par Git.

Le code Java de l'équipe va dans :

```text
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
```

Les exemples officiels sont dans :

```text
FtcRobotController/src/main/java/org/firstinspires/ftc/robotcontroller/external/samples/
```

## Compiler sans envoyer au robot

Depuis la racine du dépôt :

```sh
./scripts/compiler-robot.sh
```

Le script utilise `JAVA_HOME` s'il est défini. Sinon, il cherche le JDK d'Android
Studio dans `~/.local/opt/android-studio/jbr` ou `/opt/android-studio/jbr`, puis
utilise le Java système s'il est disponible. Pour une installation ailleurs,
définir `JAVA_HOME` vers son dossier `jbr`. Garder le SDK configuré dans Android
Studio ou définir `ANDROID_HOME` si son emplacement est différent.

Le premier lancement télécharge les dépendances du projet. L'application produite
se trouve dans `TeamCode/build/outputs/apk/debug/TeamCode-debug.apk`.
Cette commande ne lance aucun transfert ni aucune commande sur le robot.

Pour un essai matériel, connecter le Control Hub, vérifier les noms des moteurs,
capteurs et servos avec l'équipe, puis utiliser Android Studio pour le déploiement.

## Collaborer avec GitHub

L'accès **en écriture** suffit pour envoyer des branches et proposer des changements.
Les droits administrateur servent à gérer les accès et les réglages du dépôt.

Pour chaque changement, créer une branche de travail, enregistrer des commits et
envoyer cette branche vers `origin`, le dépôt de Louis. Ouvrir ensuite une demande
de fusion (*pull request*) vers **Louis-Beaunoyer/FtcRobotController : master**.
Cela permet à l'équipe de relire le changement avant de l'intégrer.

Si un dépôt distant `upstream` existe, il sert à suivre le SDK officiel FIRST.
Les changements propres au robot doivent être proposés au dépôt de l'équipe.

Louis peut protéger `master` et demander une approbation avant fusion. C'est une
amélioration du travail d'équipe ; elle n'empêche pas de préparer le code localement.

## Ce qui reste sur chaque ordinateur

Android Studio, le SDK/JDK, les identifiants GitHub et les réglages personnels de
Git sont locaux. `local.properties`, les dossiers de compilation, les caches et les
APK générés sont déjà exclus par le `.gitignore` du projet. Ils n'ont pas à être
ajoutés aux commits.

## Références

- [Version officielle FTC v12.0](https://github.com/FIRST-Tech-Challenge/FtcRobotController/releases/tag/v12.0)
- [Documentation FTC](https://ftc-docs.firstinspires.org/)
- [Android Studio pour Linux](https://developer.android.com/studio/install)
- [Connexion d'un appareil Android sous Ubuntu](https://developer.android.com/studio/run/device)
- [Droits des collaborateurs GitHub](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/repository-access-and-collaboration/permission-levels-for-a-personal-account-repository)
