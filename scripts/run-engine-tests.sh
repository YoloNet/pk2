#!/usr/bin/env bash
# PokerKu — jalankan ujian enjin poker (standalone, tanpa Android/Gradle).
#
# Enjin (Card, HandEvaluator, PokerGame, BotAi) ialah Kotlin tulen, jadi ia
# boleh dikompil dengan Kotlin compiler yang terbenam dalam distribusi
# Gradle (tiada muat turun tambahan) dan dijalankan di JVM biasa.
#
# Penggunaan (dari root repo):
#   bash scripts/run-engine-tests.sh
#
# Memerlukan: JDK 21 dan distribusi Gradle 9.8.2 di ~/gradle-dist
# (atau ubah GRADLE_DIST di bawah). Keluar 0 = semua ujian lulus.
set -euo pipefail
cd "$(dirname "$0")/.."

JAVA_HOME="${JAVA_HOME:-$HOME/jdk21}"
GRADLE_DIST="${GRADLE_DIST:-$HOME/gradle-dist/gradle-9.8.2}"
STDLIB="$GRADLE_DIST/lib/kotlin-stdlib-2.4.10.jar"
CP=$(ls "$GRADLE_DIST"/lib/*.jar | tr '\n' ':')

OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT

"$JAVA_HOME/bin/java" -cp "$CP" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
  -cp "$STDLIB" -d "$OUT" \
  app/src/main/java/com/pokerku/app/engine/Card.kt \
  app/src/main/java/com/pokerku/app/engine/HandEvaluator.kt \
  app/src/main/java/com/pokerku/app/engine/PokerGame.kt \
  app/src/main/java/com/pokerku/app/engine/BotAi.kt \
  scripts/EngineTest.kt

"$JAVA_HOME/bin/java" -cp "$OUT:$STDLIB" EngineTestKt
