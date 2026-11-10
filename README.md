# PokerKu — Texas Hold'em Offline (Android Native)

Aplikasi Android **native sepenuhnya** (Kotlin + Jetpack Compose + Material 3)
— permainan poker Texas Hold'em lawan 3 bot AI, **100% offline**. Bukan
WebView, bukan aplikasi web.

- **Nama:** PokerKu · **applicationId:** `com.pokerku.app`
- **Versi:** 1.0.1 (versionCode 1) · **minSdk 24** (Android 7.0) · **targetSdk 36**
- **Wang mainan (play money) sahaja** — tiada duit sebenar, tiada pembelian,
  tiada akaun, tiada login, tiada iklan, tiada analytics, tiada tracking.
- **Offline sepenuhnya** — manifest TIADA keizinan langsung (tiada INTERNET pun).
  Data safety Play Store: **"No data collected"**.

## Ciri

- **Texas Hold'em lengkap, 4 pemain (anda + 3 bot):** blinds 10/20, stack mula
  1,000, dealer button berpusing, pusingan pre-flop / flop / turn / river,
  aksi check / call / bet / raise / fold / all-in, side pot bila ada all-in,
  showdown dengan penilaian tangan 7 kad penuh.
- **3 bot AI dengan personaliti berbeza:**
  - **vic** — Ketat (main tangan kuat sahaja, jarang bluff)
  - **Masta** — Longgar (main banyak tangan, kerap call)
  - **Ayam** — Agresif (kerap raise, kerap bluff)
- **Meja permainan:** kad dilukis dengan Compose (tiada imej), community card,
  penanda dealer, highlight kad pemenang, nama tangan pemenang, butang pantas
  (½ Pot, Pot, All-In), slider raise, rebuy automatik ke 1,000 bila chip habis.
  Susun atur selamat edge-to-edge: header di bawah status bar,
  butang aksi di atas navigation bar sistem (statusBarsPadding /
  navigationBarsPadding) — tiada pertindihan pada skrin tinggi.
- **Avatar bot (gambar sebenar, bukan huruf):** vic, Masta dan Ayam kini
  dipaparkan dengan potret AI (latar hijau felt). Imej dibenamkan sebagai
  Base64 dalam `ui/Avatars.kt` kerana snapshot push tidak boleh push fail
  binari — imej asal 256×256 disimpan di `~/workspace/pk2-avatars/`
  (avatar-vic.jpg, avatar-masta.jpg, avatar-ayam.jpg); untuk ganti dengan
  drawable sebenar, letak PNG dalam `res/drawable` dan guna
  `painterResource()` dalam `BotAvatar`.
- **Dialog profil opponent:** klik avatar/nama bot di meja untuk melihat
  profilnya — gambar, nama, personaliti, **Biggest Pot Won** (pot terbesar
  dimenangi sesi ini) dan **Highest Chips Held** (chip tertinggi pernah
  dipegang sesi ini). Statistik bot dikira peringkat sesi sahaja (reset bila
  aplikasi dibuka semula); statistik pemain utama kekal disimpan persistent
  seperti sedia ada (Statistik).
- **Skrin:** Menu Utama, Meja, Cara Main (peraturan + ranking tangan),
  Statistik (tangan dimain/dimenang, pot terbesar, tangan terbaik, cip bersih),
  Tetapan (bahasa **English lalai** — BM boleh dipilih semula, kelajuan permainan).
- **Simpanan tempatan (DataStore):** baki chip, statistik, tetapan — kekal
  selepas aplikasi ditutup; tiada data keluar dari telefon.

## Enjin poker — sudah diuji

Enjin (`app/src/main/java/com/pokerku/app/engine/`) ialah Kotlin tulen dan
telah diuji dengan **8,578 semakan automatik — 0 gagal**, termasuk:

- Penilai tangan: royal flush, straight flush, quads, full house, flush,
  straight (termasuk roda A-2-3-4-5), trips, two pair, pair, high card,
  semua tiebreak (kicker), dan kes seri.
- Side pot: pembinaan pot berlapis disahkan dengan jumlah tepat.
- Simulasi 300 tangan penuh (bot + dasar manusia): konservasi chip
  (jumlah kekal 4,000), setiap aksi disahkan sah, tiada gelung tak berpenghujung.
- Putaran dealer 0→1→2→3, dan kemenangan fold dengan matematik chip tepat.

Jalankan semula ujian (tanpa Android/Gradle — guna Kotlin compiler dalam
distribusi Gradle):

```bash
bash scripts/run-engine-tests.sh
```

Harness ujian: `scripts/EngineTest.kt`.

## Bina di Android Studio (PC Jean)

1. `git clone https://github.com/YoloNet/pk2.git`
2. Buka folder `pk2/` dalam **Android Studio Rabbit 2** (JDK 21, SDK API 36).
3. Tunggu Gradle sync selesai, kemudian **Run** ke telefon.
4. Uji: Main → buat raise/call/fold → showdown → Tangan Seterusnya →
   klik avatar vic / Masta / Ayam (dialog profil tunjuk 2 statistik) →
   tukar bahasa ke BM dan kembali ke EN → tukar kelajuan → semak Statistik.
   Semak juga: header tidak bertindih status bar, butang Lipat/Panggil/Naikkan
   tidak dipotong navigation bar.

Nota bahasa: English kini bahasa lalai untuk pemasangan baharu. Jika telefon
anda sudah pernah menyimpan pilihan BM sebelum ini, bahasa itu dikekalkan
sehingga anda tukar sendiri dalam Tetapan (pilihan yang disimpan mengatasi
lalai baharu).

## Sejarah perubahan

- **v1.0.1 (UI fix, 10 Nov 2026):** Baiki susun atur meja (status/nav bar
  insets — header tidak lagi bertindih status bar, butang aksi tidak lagi
  dipotong navigation bar; padding tempat duduk bot ditambah); bahasa lalai
  ditukar ke English (BM kekal sebagai pilihan); avatar bot baharu (gambar
  AI, Base64 dalam kod); dialog profil opponent dengan Biggest Pot Won &
  Highest Chips Held (statistik sesi).
- **v1.0.0 (10 Nov 2026):** Versi pertama — Texas Hold'em offline lawan bot.

Tiada config untuk diisi, tiada backend, tiada kunci — clone dan Run sahaja.

## Sedia Play Store

- [x] targetSdk 36, format release **AAB**, versionCode 1
- [x] Kotlin sahaja, tiada `.so` — patuh 64-bit & 16KB page size automatik
- [x] Tiada keizinan dalam manifest; tiada data dikumpul
- [x] Ikon adaptif XML (tiada PNG dalam repo)
- [x] Tiada iklan, tiada analytics, tiada fake ID, tiada kunci sensitif
- [x] Wang mainan sahaja — dilabel jelas dalam aplikasi

Langkah kemudian (bila hendak publish): keystore sendiri + build AAB
(`Build → Generate Signed App Bundle`), aset Play Store
(`python3 scripts/make-assets.py` — ikon 512 + feature graphic),
dan isi borang Data Safety ("No data collected") + kategori kandungan
(permainan simulasi perjudian — wang mainan sahaja).

## Struktur

```
app/src/main/java/com/pokerku/app/
  MainActivity.kt            — Activity utama, edge-to-edge, 5 skrin
  data/PokerStore.kt         — DataStore (chip, statistik, tetapan)
  engine/
    Card.kt                  — kad + dek 52
    HandEvaluator.kt         — penilai tangan 5–7 kad
    PokerGame.kt             — mesin keadaan permainan + side pot + showdown
    BotAi.kt                 — AI bot (Chen pre-flop, draw post-flop)
  i18n/Strings.kt            — teks BM + English (English lalai)
  ui/
    Avatars.kt               — avatar bot (Base64 JPEG 256×256)
    PokerViewModel.kt        — orkestra game + giliran bot + simpanan
    theme/Theme.kt           — palet felt hijau + emas
    components/PokerComponents.kt — kad, tempat duduk, lencana cip
    screens/                 — Menu, Table, HowTo, Stats, Settings
scripts/
  EngineTest.kt              — harness ujian enjin (8,578 semakan)
  run-engine-tests.sh        — jalankan ujian enjin
  make-assets.py             — jana ikon 512 + feature graphic Play Store
```

## Had yang diketahui (jujur)

- Build **belum disahkan** dengan Gradle dalam VM ini (daemon loopback TCP
  disekat — had yang sama seperti projek KebunKu). Build pertama MESTI di PC
  Jean; laporkan sebarang ralat dengan teks penuh Build output.
- Enjin poker disahkan melalui ujian standalone (lihat atas), tetapi UI
  Compose hanya disemak melalui bacaan kod — semak visual di telefon sebenar.
- `gradlew` / `gradlew.bat` tiada dalam repo (dijana oleh Android Studio
  semasa sync pertama) — sama seperti kuv2 selepas pembetulan Jean.
