# Play Store Görselleri

Bu klasördeki dosyalar Google Play Console'daki "Mağaza kaydı" bölümüne yüklenir.
Uygulama içindeki launcher ikonu da aynı tasarımdan üretildi, böylece mağaza ile
telefondaki simge birbirini tutuyor.

| Dosya | Boyut | Play Console'daki yeri |
|---|---|---|
| `ic_playstore_512.png` | 512×512 | Uygulama simgesi |
| `feature_graphic_1024x500.png` | 1024×500 | Öne çıkan grafik |
| `icon.svg` | vektör | Simgenin düzenlenebilir kaynağı |
| `feature_graphic.svg` | vektör | Öne çıkan grafiğin kaynağı |

## Tasarım

Uygulamanın kendi paletinden geliyor: krem zemin (`#FDF0DE`), turuncu geri sayım
halkası (`#F79233`) ve halkanın ucunda süre bitişini simgeleyen kırmızı nokta
(`#E53935`). Halka motifi zamanlayıcı ekranındaki ilerleme halkasının aynısı.

## SVG'den PNG üretmek

Tasarımı değiştirirsen PNG'leri şöyle yeniden üretebilirsin:

```bash
pip install cairosvg
python3 -c "import cairosvg; cairosvg.svg2png(url='icon.svg', write_to='ic_playstore_512.png', output_width=512, output_height=512)"
python3 -c "import cairosvg; cairosvg.svg2png(url='feature_graphic.svg', write_to='feature_graphic_1024x500.png', output_width=1024, output_height=500)"
```

## Hâlâ gereken

- [ ] **Ekran görüntüsü** (en az 2 adet, telefon) — emülatörde yan paneldeki
      kamera düğmesiyle alınabilir. Önerilen kareler: seviye seçimi, çalışan
      zamanlayıcı (halka yarı dolu), "Yumurta hazır!" ekranı.
