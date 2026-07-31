# Play Store Yayını İçin Kontrol Listesi

## ✅ Tamamlanan İşlemler (kod tarafı hazır)

1. ✅ **Application ID değiştirildi**: `com.ferhatcangeyik.eggtimer` (com.example reddedilirdi)
2. ✅ **Test butonu kaldırıldı**: Üretim arayüzünde test kodu yok
3. ✅ **Release signing yapılandırıldı**: `keystore.properties` dosyasından okunuyor
   (dosya yoksa release imzasız derlenir, debug etkilenmez)
4. ✅ **Privacy Policy sayfası eklendi**: `docs/privacy-policy.html` (TR + EN)
5. ✅ **Privacy Policy URL güncellendi**: `strings.xml` → GitHub Pages adresi
6. ✅ **Target SDK**: 35
7. ✅ **ProGuard/R8 aktif**: Release build için minify ve shrink resources açık
8. ✅ **Backup / data extraction rules yapılandırıldı**
9. ✅ **Keystore git koruması**: `.gitignore` içinde `*.jks` ve `keystore.properties`

## ⚠️ Senin Yapman Gerekenler

### 1. Keystore oluştur (ZORUNLU — kendi bilgisayarında)

Proje kök dizininde:

```bash
keytool -genkey -v -keystore eggtimer-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias eggtimer
```

Sonra `keystore.properties.example` dosyasını `keystore.properties` adıyla kopyala
ve şifreleri doldur. İkisi de git'e girmez.

> **KRİTİK**: `eggtimer-release.jks` dosyasını güvenli bir yere yedekle
> (Google Drive, USB vb.). Kaybedersen uygulamayı bir daha güncelleyemezsin!

### 2. GitHub Pages'i aktifleştir (Privacy Policy için)

Branch master'a merge edildikten sonra:
GitHub → repo **Settings** → **Pages** → Source: **Deploy from a branch** →
Branch: **master**, klasör: **/docs** → Save

Birkaç dakika içinde şu adres canlı olur:
`https://t-t-chopper.github.io/Egg_Timer/privacy-policy.html`

### 3. Release AAB oluştur ve test et

```bash
./gradlew bundleRelease
```
AAB: `app/build/outputs/bundle/release/app-release.aab`

Cihazda test için APK: `./gradlew assembleRelease`

**Kontrol edilecekler**:
- [ ] Uygulama açılıyor, dil seçimi çalışıyor
- [ ] Timer çalışıyor, halka doğru doluyor
- [ ] Alarm sesi ve titreşim çalışıyor
- [ ] Test butonu GÖRÜNMÜYOR
- [ ] Zamanlayıcı çalışırken başka uygulamaya geçilince süre işlemeye devam ediyor
- [ ] Uygulama görevler listesinden kapatılıp yeniden açılınca süre kaldığı yerden sürüyor
- [ ] Arka plandayken süre dolunca bildirim geliyor ve alarm çalıyor

### 4. Hassas izin bildirimi (USE_EXACT_ALARM)

Zamanlayıcı arka plandayken alarmın dakikası dakikasına çalması için
`USE_EXACT_ALARM` izni kullanılıyor. Play Console bu izin için kısa bir
gerekçe isteyebilir:

> **Politika → Uygulama içeriği → Tam zamanlı alarmlar**

Yazılabilecek gerekçe:

> Uygulamanın temel işlevi bir pişirme zamanlayıcısıdır. Kullanıcı yumurtanın
> pişme süresini seçer ve süre dolduğunda uyarılması gerekir. Birkaç dakika
> geciken bir alarm yumurtanın fazla pişmesine yol açacağı için tam zamanlı
> alarm gereklidir. İzin yalnızca kullanıcının kendi başlattığı zamanlayıcı
> için kullanılır.

> Not: İzin verilmezse uygulama yaklaşık alarma düşer ve çalışmaya devam eder.

### 5. Play Console hesabı ve listing

- [ ] Google Play Developer hesabı (tek seferlik 25 USD): https://play.google.com/console
- [ ] Uygulama adı: Egg Timer / Yumurta Zamanlayıcısı
- [ ] Kısa açıklama (80 karakter) ve uzun açıklama
- [ ] Ekran görüntüleri (en az 2 adet, telefon)
- [ ] Uygulama ikonu 512x512 px PNG
- [ ] Feature graphic 1024x500 px
- [ ] Kategori: Araçlar (Tools) veya Yemek ve İçecek
- [ ] İçerik derecelendirmesi anketi (veri toplamıyor → herkes için uygun)
- [ ] Privacy Policy URL: yukarıdaki GitHub Pages adresi
- [ ] İletişim e-postası

### 6. Her güncellemede

- `versionCode` +1 artır (şu an: 1)
- `versionName` güncelle (şu an: "1.0")

## Yardımcı Komutlar

```bash
./gradlew bundleRelease    # Play Store için AAB (önerilen)
./gradlew assembleRelease  # Test için APK
./gradlew clean            # Temizlik
```
