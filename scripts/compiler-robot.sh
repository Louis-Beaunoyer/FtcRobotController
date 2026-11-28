#!/usr/bin/env bash
# Compile le projet de l'équipe ; aucune installation sur un robot.
set -euo pipefail

project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir"

if [[ -z "${JAVA_HOME:-}" ]]; then
    for runtime_dir in "$HOME/.local/opt/android-studio/jbr" /opt/android-studio/jbr; do
        if [[ -x "$runtime_dir/bin/java" ]]; then
            export JAVA_HOME="$runtime_dir"
            break
        fi
    done
fi

if [[ -n "${JAVA_HOME:-}" ]]; then
    if [[ ! -x "$JAVA_HOME/bin/java" ]]; then
        printf '%s\n' "JAVA_HOME ne contient pas de Java exécutable : $JAVA_HOME" >&2
        exit 1
    fi
    export PATH="$JAVA_HOME/bin:$PATH"
elif ! command -v java >/dev/null 2>&1; then
    printf '%s\n' 'Java est introuvable. Définis JAVA_HOME vers le dossier jbr de ton Android Studio.' >&2
    exit 1
fi

# Un local.properties créé par Android Studio reste prioritaire pour Gradle.
if [[ -z "${ANDROID_HOME:-}" && -d "$HOME/Android/Sdk" ]]; then
    export ANDROID_HOME="$HOME/Android/Sdk"
fi

exec bash ./gradlew --no-daemon :TeamCode:assembleDebug "$@"
