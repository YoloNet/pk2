# PokerKu native — peraturan ProGuard untuk build release (AAB).
# Aplikasi ini Kotlin + Compose sahaja; tiada refleksi, tiada serialization
# tersuai, tiada library pihak ketiga yang perlukan peraturan khas.
-keepattributes *Annotation*, InnerClasses
