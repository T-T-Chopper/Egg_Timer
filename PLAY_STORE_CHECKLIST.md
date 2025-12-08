# Play Store Yayını İçin Kontrol Listesi

## ✅ Tamamlanan İşlemler

1. ✅ **Target SDK güncellendi**: 36 → 35 (daha stabil)
2. ✅ **ProGuard/R8 aktif edildi**: Release build için minify ve shrink resources açıldı
3. ✅ **ProGuard kuralları eklendi**: Compose ve enum sınıfları korunuyor
4. ✅ **Backup rules yapılandırıldı**: SharedPreferences yedekleniyor
5. ✅ **Data extraction rules yapılandırıldı**: Cloud backup ve device transfer için
6. ✅ **Privacy Policy URL placeholder eklendi**: `strings.xml` içinde

## ⚠️ Yapılması Gerekenler

### 1. Application ID Değiştirme (ZORUNLU)
**Mevcut**: `com.example.eggtimer`  
**Yapılacak**: Gerçek bir domain ile değiştirin (örn: `com.yourname.eggtimer` veya `com.yourcompany.eggtimer`)

**Dosya**: `app/build.gradle.kts`
```kotlin
applicationId = "com.yourname.eggtimer"  // Değiştirin
namespace = "com.yourname.eggtimer"      // Değiştirin
```

**ÖNEMLİ**: Application ID değiştirdikten sonra:
- `package` adını da güncelleyin (`MainActivity.kt` ve diğer Kotlin dosyaları)
- Projeyi temizleyin: `./gradlew clean`
- Yeniden build edin

### 2. Release Signing Config (ZORUNLU)
**Dosya**: `app/build.gradle.kts`

**Adımlar**:
1. Keystore dosyası oluşturun:
```bash
keytool -genkey -v -keystore eggtimer-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias eggtimer
```

2. `app/build.gradle.kts` dosyasındaki `signingConfigs` bölümünü doldurun:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../eggtimer-release.jks")
        storePassword = "your-store-password"
        keyAlias = "eggtimer"
        keyPassword = "your-key-password"
    }
}
```

3. `buildTypes.release` içinde şu satırı uncomment edin:
```kotlin
signingConfig = signingConfigs.getByName("release")
```

**GÜVENLİK**: 
- Keystore dosyasını `.gitignore`'a ekleyin
- Şifreleri `local.properties` veya environment variables'da saklayın
- Keystore dosyasını güvenli bir yerde yedekleyin (kaybederseniz güncelleme yapamazsınız!)

### 3. Privacy Policy URL (ZORUNLU)
**Dosya**: `app/src/main/res/values/strings.xml`

Play Store, kullanıcı verisi toplayan uygulamalar için Privacy Policy URL'i zorunlu kılar. 

**Yapılacaklar**:
1. Bir privacy policy sayfası oluşturun (GitHub Pages, Google Sites, veya kendi web siteniz)
2. `strings.xml` içindeki URL'i güncelleyin:
```xml
<string name="privacy_policy_url">https://yourwebsite.com/privacy-policy</string>
```

**Not**: Bu uygulama sadece dil tercihini (SharedPreferences) saklıyor. Basit bir privacy policy yeterli olacaktır.

### 4. App Icon Kontrolü
**Mevcut**: Launcher icon'lar mevcut (mipmap klasörlerinde)

**Play Store için gerekli**:
- **512x512 px** PNG (feature graphic için)
- **1024x1024 px** PNG (Play Store listing için)

**Kontrol edin**:
- Icon'un tüm ekran boyutlarında düzgün göründüğünden emin olun
- Test cihazlarında icon'un düzgün göründüğünü doğrulayın

### 5. Version Code ve Version Name
**Mevcut**: 
- `versionCode = 1`
- `versionName = "1.0"`

**İlk yayın için uygun**. Her güncellemede `versionCode`'u artırın.

### 6. Test ve Doğrulama

**Release build testi**:
```bash
./gradlew assembleRelease
```

**Kontrol edilecekler**:
- [ ] Uygulama açılıyor
- [ ] Dil seçimi çalışıyor
- [ ] Timer çalışıyor
- [ ] Alarm sesi ve titreşim çalışıyor
- [ ] Tüm ekranlar düzgün görünüyor
- [ ] Farklı ekran boyutlarında test edildi
- [ ] Farklı Android versiyonlarında test edildi (minSdk 24 = Android 7.0)

### 7. Play Store Console Hazırlığı

**Gerekli bilgiler**:
- [ ] Uygulama adı (kısa ve uzun)
- [ ] Açıklama (kısa ve uzun)
- [ ] Ekran görüntüleri (en az 2, farklı ekran boyutları için)
- [ ] Feature graphic (1024x500 px)
- [ ] Icon (512x512 px)
- [ ] Kategori seçimi
- [ ] İçerik derecelendirmesi
- [ ] Privacy Policy URL
- [ ] İletişim bilgileri (e-posta)

### 8. Ek Öneriler

**Performans**:
- Release build'de ProGuard aktif, bu iyi
- `isShrinkResources = true` ile kullanılmayan kaynaklar kaldırılıyor

**Güvenlik**:
- Vibrator izni mevcut ✅
- Gereksiz izin yok ✅
- `android:exported="true"` sadece MainActivity'de (doğru) ✅

**Kullanıcı Deneyimi**:
- Edge-to-edge desteği var ✅
- RTL desteği var ✅
- Material 3 kullanılıyor ✅

## Son Kontrol Listesi

Yayınlamadan önce:
- [ ] Application ID değiştirildi
- [ ] Release signing config eklendi ve test edildi
- [ ] Privacy Policy URL güncellendi
- [ ] Release build başarıyla oluşturuldu
- [ ] Release build test edildi
- [ ] Tüm ekran görüntüleri hazır
- [ ] Play Store listing bilgileri hazır
- [ ] Keystore dosyası güvenli bir yerde yedeklendi

## Yardımcı Komutlar

**Release APK oluşturma**:
```bash
./gradlew assembleRelease
```
APK: `app/build/outputs/apk/release/app-release.apk`

**Release AAB oluşturma** (Play Store için önerilen):
```bash
./gradlew bundleRelease
```
AAB: `app/build/outputs/bundle/release/app-release.aab`

**Temizleme**:
```bash
./gradlew clean
```

## Notlar

- **AAB formatı önerilir**: Play Store, Android App Bundle (AAB) formatını tercih eder
- **Keystore güvenliği**: Keystore dosyasını ve şifrelerini kesinlikle kaybetmeyin
- **Test**: Mümkünse beta test kanalı kullanın (Play Store Console'da)
- **Güncellemeler**: Her güncellemede `versionCode`'u artırmayı unutmayın

