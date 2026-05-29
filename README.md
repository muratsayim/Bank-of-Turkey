# 🏦 Bank of Turkey - Masaüstü Bankacılık Uygulaması

Bu proje, Java 17 kullanılarak geliştirilmiş, modern görünümlü (FlatLaf destekli) bir **Masaüstü Bankacılık Uygulamasıdır.** Projede veri tabanı yerine hafif ve taşınabilir bir dosya tabanlı (JSON) veri depolama mimarisi kullanılmıştır.

---

## ✨ Özellikler

*   **Modern Swing Arayüzü (FlatLaf):** Eski görünümlü Java Swing arayüzleri yerine, modern işletim sistemleriyle uyumlu şık ve karanlık/aydınlık tema destekli FlatLaf tasarımı kullanılmıştır.
*   **Çift Hesap Türü Desteği:**
    *   **Vadesiz Hesap (Checking Account):** Günlük harcamalar ve para transferleri için.
    *   **Vadeli Hesap (Savings Account):** Birikim yapmak ve faiz getirisi hesaplamak için.
*   **Kapsamlı Bankacılık İşlemleri:**
    *   Para Yatırma (Deposit) ve Para Çekme (Withdrawal)
    *   Hesaplar Arası Havale/EFT (Transfer)
    *   Yeni vadesiz/vadeli hesap açılışı
    *   Tüm hesap hareketleri geçmişinin (Transaction History) listelenmesi ve filtrelenmesi.
*   **JSON Veri Depolama (Gson):** Müşteri verileri ve hesap hareketleri `data/` klasörü altında `customers.json` ve `transactions.json` olarak şifreli/güvenli şekilde saklanır.
*   **Güvenli Giriş & Kayıt:** Müşterilerin güvenli şifreleme ile üye olması ve giriş yapması sağlanır.

---

## 📁 Proje Yapısı

```text
banking-app/
├── src/main/java/com/bank/
│   ├── App.java                 # Uygulama ana giriş noktası (Main Class)
│   ├── model/                   # Veri modelleri (Account, Customer, Transaction, vb.)
│   ├── repository/              # JSON veri okuma ve yazma katmanı (DataRepository)
│   ├── service/                 # Bankacılık iş mantığı (BankingService)
│   └── ui/                      # Arayüz panelleri (Dashboard, Login, Register, MainFrame)
├── data/                        # Yerel veritabanı (JSON dosyaları - Git tarafından yoksayılır)
├── pom.xml                      # Maven bağımlılık ve derleme yapılandırması
├── run.bat                      # Uygulamayı çift tıklayarak çalıştıran Windows betiği
└── .gitignore                   # Hassas veri dosyalarının GitHub'a gitmesini engelleyen filtre
```

---

## 🛠️ Kurulum ve Çalıştırma

### Gereksinimler
*   Bilgisayarınızda **Java 17 (JDK)** yüklü olmalıdır.
*   Proje yapılandırması için **Maven** gereklidir (Gerekirse lokal Maven paketi kullanılabilir).

### Uygulamayı Çalıştırma

1.  **Windows'ta (Çift Tıklayarak):**
    Proje klasörü içindeki **`run.bat`** dosyasına çift tıklayarak uygulamayı anında başlatabilirsiniz.

2.  **Terminal/Konsol Üzerinden:**
    Proje ana klasöründe terminali açıp aşağıdaki Maven komutunu çalıştırmanız yeterlidir:
    ```bash
    mvn exec:java
    ```

3.  **Projeyi Derleme (Build):**
    Projeyi derlemek ve çalıştırılabilir bir JAR dosyası oluşturmak için:
    ```bash
    mvn clean package
    ```

---

## 🔒 Güvenlik Uyarıları

*   Müşteri bakiyeleri, şifreler ve işlem kayıtları gibi hassas bilgileri içeren `data/` klasörü altındaki JSON dosyaları, güvenlik nedeniyle `.gitignore` dosyası aracılığıyla GitHub'a **yüklenmeyecek şekilde** yapılandırılmıştır.
