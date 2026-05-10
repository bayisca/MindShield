# 🛡️ MindShield: Anonim Ruh Sağlığı & Psikolojik Destek Platformu

**MindShield**, modern psikolojik destek yaklaşımlarını teknolojiyle birleştiren, kullanıcı gizliliğini en üst seviyede tutan Java tabanlı bir masaüstü uygulamasıdır. Kullanıcıların gerçek kimliklerini saklayarak uzmanlardan destek almasını, toplulukla etkileşime girmesini ve kişisel gelişim süreçlerini takip etmesini sağlar.

---

## 🚀 Temel Özellikler (Modüller)

### 👤 Persona Sistemi (Üyelik & Güvenlik)
*   **Tam Anonimlik:** Kullanıcılar sadece "Persona" adıyla sisteme kayıt olur.
*   **Rol Tabanlı Erişim:** Danışan, Danışman ve Yönetici (Admin) için farklı arayüzler ve yetkiler.
*   **Güvenli Giriş:** SQL tabanlı kimlik doğrulama ve session yönetimi.

### 🏠 Akıllı Dashboard (Ana Ekran)
*   **Dinamik Kısayollar:** En son okunan bloglar, gelen mesajlar ve son etkinliklere hızlı erişim.
*   **Hoş Geldin Paneli:** Kullanıcıyı ismi ve günlük ilham verici sözlerle karşılayan şık header.

### 📝 JourShield (Dijital Günlük)
*   **Ruh Hali Takibi:** Her gün için bir "Mood" seçimi ve detaylı günlük yazımı.
*   **Kişisel Arşiv:** Tüm eski günlük girişlerinin tarihe göre listelenmesi ve yönetimi.
*   **Tam Gizlilik:** Günlük girişleri veritabanında kullanıcıya özel saklanır, danışmanlar dahil kimse göremez.

### 🎵 SoulShield (Meditasyon & Odaklanma)
*   **Arka Plan Oynatıcı:** Meditasyon müzikleri ve rahatlatıcı doğa sesleri.
*   **Favori Sistemi:** Beğenilen müziklerin hızlı erişim listesine eklenmesi.

### 🗨️ Forum & Blog Modülleri
*   **Uzman Blogları:** Danışmanlar tarafından yazılan profesyonel makaleler.
*   **Topluluk Forumu:** Kategorize edilmiş tartışma başlıkları ve dayanışma ortamı.
*   **Etkileşim:** Yazılara yorum yapma ve başlık açma özellikleri.

### 👮 Yönetici Paneli (Moderasyon)
*   **Danışman Onayı:** Sisteme kayıt olan danışmanların yetki onayı.
*   **Sistem Logları:** Uygulama içindeki önemli olayların (hata, giriş vb.) takibi.

---

## 🛠️ Teknik Mimari & Teknolojiler

### Teknoloji Yığını
*   **Dil:** Java 21
*   **Arayüz:** JavaFX 21 & FXML
*   **Stil:** Custom CSS (Indigo-Teal Modern UI)
*   **Veritabanı:** H2 Database Engine (Local SQL Storage)
*   **Bağlantı:** JDBC (Java Database Connectivity)

### Proje Yapısı (Folder Structure)
```text
MindShield/
├── src/main/java/com/mindshield/
│   ├── dao/           # Veritabanı Erişim Katmanı (SQL İşlemleri)
│   ├── models/        # Veri Modelleri (User, Blog, Post vb.)
│   ├── services/      # İş Mantığı (Logic Layer)
│   └── ui/            # JavaFX Controller ve UI Sınıfları
├── src/main/resources/
│   ├── logos/         # İkon ve Marka Görselleri
│   ├── Home.fxml      # Sayfa Tasarımları
│   └── style.css      # Global Tasarım ve Animasyonlar
└── pom.xml            # Maven Bağımlılıkları
```

### Veritabanı Şeması
*   **`users`:** `id`, `username`, `password`, `role`, `profession`, `created_at`
*   **`blog_posts`:** `id`, `author_id`, `title`, `content`, `category`
*   **`journal_entries`:** `id`, `user_id`, `title`, `body`, `mood`, `entry_date`
*   **`forum_topics`:** `id`, `author_id`, `title`, `category`, `content`

---

## 💎 Yazılım Prensipleri (OOP)

Bu proje akademik seviyede temiz kod ve OOP prensiplerine uygun olarak geliştirilmiştir:
1.  **Encapsulation (Kapsülleme):** Tüm modeller (Model classes) private field'lar ve kontrollü getter/setter metodları ile yönetilir.
2.  **Inheritance (Kalıtım):** `BaseUser` sınıfı; `Admin`, `Counselor` ve `StandardUser` sınıflarına temel oluşturur.
3.  **Abstraction (Soyutlama):** `UserDao`, `PostDao` gibi arayüzler (Interfaces) kullanılarak veritabanı teknolojisi iş mantığından soyutlanmıştır.
4.  **Interface Segregation:** Modüller (Forum, Mesaj, Günlük) birbirinden bağımsız servisler ve arayüzler üzerinden haberleşir.

---

## ⚙️ Kurulum ve Çalıştırma

1.  **JDK Kurulumu:** Bilgisayarınızda Java 21 veya üzeri kurulu olmalıdır.
2.  **Derleme:**
    ```bash
    mvn clean install
    ```
3.  **Çalıştırma:**
    ```bash
    mvn javafx:run
    ```
    *Not: Veritabanı ilk çalıştırmada otomatik olarak oluşturulur (`DatabaseInitializer` sınıfı sayesinde).*

---

## 🎯 Değerlendirme İçin Notlar
*   Uygulama **Maximized (Tam Ekran)** modunda açılacak şekilde optimize edilmiştir.
*   Tüm `dat` dosyası bağımlılıkları kaldırılmış, **%100 SQL** kalıcılığına geçilmiştir.
*   **UI/UX:** Indigo renk paleti, animasyonlu typing efektleri ve responsive grid düzenleri kullanılmıştır.
