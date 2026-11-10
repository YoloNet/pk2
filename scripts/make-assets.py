#!/usr/bin/env python3
"""PokerKu native — jana aset imej untuk Play Store (v1.0.0).

PNG tidak boleh dihantar melalui snapshot push GitHub, jadi aset binari
TIDAK di-commit dalam repo ini. Sebaliknya, skrip ini menjana semula aset
daripada logo vektor yang sama seperti ikon (app/src/main/res/drawable/
ic_logo.xml) dengan PIL sahaja (tiada rangkaian, tiada kebergantungan luar):

  play-assets/icon-512.png           — ikon Play Store 512x512
  play-assets/feature-graphic.png    — grafik ciri Play Store 1024x500

Ikon pelancar dalam aplikasi pula ialah XML adaptif (mipmap-anydpi-v26/
ic_launcher.xml + drawable/ic_logo.xml + drawable/ic_launcher_background.xml)
— tiada PNG diperlukan untuk build. Langkah ini hanya untuk fail yang
dimuat naik ke Play Console.

Penggunaan:
  python3 scripts/make-assets.py [--out DIR]
"""

import argparse
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

FELT_GELAP = (7, 42, 32)        # #072A20
FELT = (11, 61, 46)             # #0B3D2E
KRIM = (247, 244, 236)          # #F7F4EC
EMAS = (192, 138, 26)           # #C08A1A
EMAS_TERANG = (231, 196, 106)
PUTIH = (255, 255, 255)


def cari_fon_bold(saiz):
    """Cari fon sans bold yang ada dalam sistem (Linux / Windows / macOS)."""
    calon = [
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
        "/usr/share/fonts/TTF/DejaVuSans-Bold.ttf",
        "C:/Windows/Fonts/arialbd.ttf",
        "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
    ]
    for p in calon:
        try:
            return ImageFont.truetype(p, saiz)
        except OSError:
            continue
    return ImageFont.load_default()


def lukis_cip(draw, cx, cy, r):
    """Lukis cip poker: cakera krim, takuk emas, cakera dalam hijau, berlian emas."""
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=KRIM)
    # 6 takuk emas di keliling.
    import math
    for i in range(6):
        a = math.radians(i * 60)
        x = cx + int((r - 4) * math.sin(a))
        y = cy - int((r - 4) * math.cos(a))
        draw.rectangle([x - 5, y - 5, x + 5, y + 5], fill=EMAS)
    draw.ellipse(
        [cx - int(r * 0.66), cy - int(r * 0.66), cx + int(r * 0.66), cy + int(r * 0.66)],
        fill=FELT,
    )
    draw.ellipse(
        [cx - int(r * 0.66), cy - int(r * 0.66), cx + int(r * 0.66), cy + int(r * 0.66)],
        outline=EMAS,
        width=3,
    )
    d = int(r * 0.30)
    draw.polygon([(cx, cy - d), (cx + d, cy), (cx, cy + d), (cx - d, cy)], fill=EMAS_TERANG)


def buat_ikon_512(out_dir: Path):
    img = Image.new("RGB", (512, 512), FELT_GELAP)
    draw = ImageDraw.Draw(img)
    draw.ellipse([16, 16, 496, 496], fill=FELT)
    lukis_cip(draw, 256, 256, 170)
    img.save(out_dir / "icon-512.png")
    print("icon-512.png — 512x512")


def buat_feature_graphic(out_dir: Path):
    img = Image.new("RGB", (1024, 500), FELT_GELAP)
    draw = ImageDraw.Draw(img)
    draw.ellipse([-150, -150, 400, 400], fill=FELT)
    lukis_cip(draw, 200, 250, 120)
    fon_judul = cari_fon_bold(110)
    fon_sub = cari_fon_bold(44)
    draw.text((380, 130), "PokerKu", fill=EMAS_TERANG, font=fon_judul)
    draw.text((382, 270), "Texas Hold'em Offline", fill=KRIM, font=fon_sub)
    draw.text((382, 330), "Lawan vic, Masta & Ayam", fill=KRIM, font=fon_sub)
    img.save(out_dir / "feature-graphic.png")
    print("feature-graphic.png — 1024x500")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", default="play-assets")
    args = ap.parse_args()
    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)
    try:
        buat_ikon_512(out_dir)
        buat_feature_graphic(out_dir)
    except ImportError:
        print("PIL (Pillow) tidak dijumpai. Pasang dahulu: pip install Pillow", file=sys.stderr)
        return 1
    print(f"Selesai — fail di {out_dir}/ (muat naik ke Play Console, bukan ke repo).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
