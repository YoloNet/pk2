# PokerKu — Panduan Android Studio (PC Jean)

Panduan ringkas untuk build pertama aplikasi PokerKu (native, offline).

## Keperluan

- **Android Studio Rabbit 2** (2026.2.2) atau lebih baharu
- **JDK 21** (terbina dalam Android Studio — tiada pemasangan berasingan)
- **SDK API 36** (Android 16) — pasang melalui SDK Manager jika belum ada
  (SDK Manager → SDK Platforms → tick **Android 16.0 (API 36)** → Apply)
- **Gradle 9.8.2** — wrapper dalam repo sudah tetapkannya; Android Studio
  akan muat turun automatik semasa sync pertama

## Langkah build pertama

1. **Clone repo:**
   ```bash
   git clone https://github.com/YoloNet/pk2.git
   ```
2. **Buka dalam Android Studio:** File → Open → pilih folder `pk2/`.
3. **Tunggu Gradle sync** selesai (bar bawah; pertama kali ambil masa
   beberapa minit kerana muat turun dependencies).
4. **Run ke telefon:** sambung telefon (USB debugging ON) atau guna emulator,
   kemudian tekan butang **Run ▶** (atau Shift+F10).
5. **Uji aliran penuh:**
   - Menu → **Main** → meja dibuka, anda dapat 2 kad
   - Buat **Call / Raise / Fold** — bot (vic, Masta, Ayam) bertindak selepas jeda
   - Main sampai **Showdown** — banner pemenang + nama tangan dipaparkan
   - Tekan **Tangan Seterusnya** — dealer berpusing
   - Menu → **Tetapan** — tukar bahasa (BM/English) dan kelajuan permainan
   - Menu → **Statistik** — semak tangan dimain/dimenang dan pot terbesar

## Jika sync gagal

- Pastikan **JDK 21** dipilih: File → Settings → Build, Execution,
  Deployment → Build Tools → Gradle → Gradle JDK → **21**.
- Pastikan **SDK API 36** terpasang (lihat Keperluan di atas).
- **Sync semula:** File → Sync Project with Gradle Files.
- Jika masih gagal, salin **teks penuh** ralat dari tab Build dan hantar
  kepada Lapis — jangan hantar tangkapan skrin sahaja.

## Ujian enjin (pilihan, di PC)

Enjin poker boleh diuji tanpa Android — dari terminal dalam folder repo:

```bash
bash scripts/run-engine-tests.sh
```

Memerlukan JDK 21 dan Gradle 9.8.2 di `~/gradle-dist` (atau tetapkan
`GRADLE_DIST`). Dijangka: **8,578 semakan, 0 gagal**.

## Bina AAB untuk Play Store (kemudian)

1. Build → **Generate Signed App Bundle / APK** → pilih **Android App Bundle**.
2. Buat **keystore baharu** (simpan fail `.jks` dan kata laluan di tempat
   selamat — JANGAN commit ke repo, JANGAN hilang; tanpanya anda tidak boleh
   update aplikasi di Play nanti).
3. Pilih build variant **release** → Create.
4. Aset Play Store: `python3 scripts/make-assets.py` (ikon 512 +
   feature graphic 1024×500) — muat naik ke Play Console, bukan ke repo.
5. Di Play Console: Data Safety → **No data collected**; kategori kandungan:
   permainan simulasi perjudian (wang mainan sahaja).

## Nota

- Aplikasi ini **100% offline** — tiada backend, tiada config, tiada kunci
  untuk diisi. Clone dan Run sahaja.
- `gradlew` / `gradlew.bat` dijana oleh Android Studio semasa sync pertama;
  ia tiada dalam repo dengan sengaja.
- Kod sumber: `app/src/main/java/com/pokerku/app/` — enjin di `engine/`,
  UI di `ui/screens/`, teks dwibahasa di `i18n/Strings.kt`.
