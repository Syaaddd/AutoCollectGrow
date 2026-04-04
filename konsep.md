
---

## Nama Plugin: **AutoCollect for Slimefun**

### Konsep inti
Plugin ini adalah Slimefun machine berbentuk chest yang bisa **scan area sekitarnya** secara otomatis, mengambil item dari semua container (chest, barrel, dll) dalam radius tertentu, menyimpannya ke internal storage-nya sendiri, lalu player tinggal klik tombol **Sell** di GUI untuk jual semua ke ShopGuiPlus.

---

### Sistem Tier
Setiap tier memiliki radius berbeda dan bisa di-craft menggunakan bahan Slimefun yang semakin mahal:
- **Tier 1** — Basic Collector, radius 5 blok, bahan: iron + basic circuit
- **Tier 2** — Advanced Collector, radius 10 blok, bahan: reinforced alloy + electric motor
- **Tier 3** — Elite Collector, radius 20 blok, bahan: billon ingot + advanced circuit
- **Tier 4** — Quantum Collector, radius 50 blok, bahan: pure nether alloy + quantum processor

### Cara kerja teknis
Di backend, tiap Collector punya `BukkitTask` async yang jalan setiap N tick (bisa dikonfigurasi). Task ini loop semua block dalam radius sphere, cari `InventoryHolder`, lalu pindahkan item yang cocok dengan whitelist ke internal storage di `BlockStorage` Slimefun.

### Integrasi ShopGuiPlus
Saat player klik tombol **SELL ALL**, plugin akan loop semua item di storage, cek harga lewat `ShopGuiPlus API` (`ShopGuiPlusApi.getItemPrice()`), kalkulasi total, lalu deposit ke economy via **Vault**. Ada event hook juga supaya bisa di-log atau di-cancel oleh plugin lain.

### Fitur tambahan yang bisa ditambahkan
- **Auto-sell mode** — otomatis jual tiap X menit tanpa perlu buka GUI
- **Per-item radius** — setiap jenis item bisa punya radius scan berbeda lewat config
- **Sell history log** — riwayat transaksi bisa dilihat di GUI
- **PlaceholderAPI** — bisa tampilkan total profit di scoreboard/tablist
- **Upgrade in-place** — upgrade tier langsung dari GUI tanpa cabut blok

### Struktur file project
```
src/
├── items/         ← registrasi SF items (tiap tier)
├── machines/      ← logic AutoCollectorMachine.java
├── gui/           ← ChestCollectorGUI.java
├── tasks/         ← CollectorTask.java (async scan)
├── hooks/         ← ShopGuiPlusHook.java, VaultHook.java
└── config/        ← config.yml per-item radius settings
```

---

Oke, saya update konsep dengan pom.xml yang sudah disesuaikan untuk project AutoCollect ini, mengikuti struktur dari pom.xml kamu sebagai base!Ini `pom.xml` yang sudah disesuaikan untuk project **AutoCollect**. Perubahan dari pom kamu:

**Yang dipertahankan** — struktur build (shade plugin, surefire, lombok, jsr305, GuizhanLib, Spigot API, Slimefun4 Gugu fork, Java 8 target).

**Yang ditambahkan:**

- **ShopGuiPlus** — dikomentari dengan instruksi cara install manual via `mvn install:install-file` karena tidak tersedia di repo publik Maven. Ada dua opsi: local Maven repo atau system path dari folder `/lib`.
- **Vault API** — untuk deposit uang ke economy setelah sell.
- **PlaceholderAPI** — untuk stats display di scoreboard/tablist.
- **bStats** — opsional untuk analytics, sudah dikonfigurasi relocation di shade plugin supaya tidak konflik dengan plugin lain.
- **Gugu Maven repo** — repo resmi untuk Slimefun CN fork (sebagai alternatif JitPack yang kadang lambat).
- **bStats relocation** — di blok `<relocations>` shade plugin, ini penting agar package bStats tidak bentrok dengan plugin lain yang juga shade bStats.

**Yang dihapus** — `spring-context` karena tidak relevan untuk plugin Minecraft.

> Untuk ShopGuiPlus, minta jar-nya dari SpigotMC premium resource page, lalu jalankan perintah `mvn install:install-file` yang ada di komentar pom-nya.

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <groupId>com.github.yourname</groupId>
    <artifactId>AutoCollect</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <author>YourName</author>
    </properties>

    <repositories>
        <!-- Spigot / Paper API -->
        <repository>
            <id>paper-repo</id>
            <url>https://repo.destroystokyo.com/repository/maven-public/</url>
        </repository>
        <repository>
            <id>spigot-repo</id>
            <url>https://hub.spigotmc.org/nexus/content/repositories/snapshots/</url>
        </repository>

        <!-- JitPack — untuk Slimefun4 & ShopGuiPlus (jika ada) -->
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>

        <!-- Gugu Maven (Slimefun CN fork) — release -->
        <repository>
            <id>gugu-maven-repo</id>
            <url>https://maven.norain.city/releases</url>
        </repository>

        <!-- bStats -->
        <repository>
            <id>CodeMC</id>
            <url>https://repo.codemc.io/repository/maven-public/</url>
        </repository>

        <!-- Vault (economy) -->
        <repository>
            <id>vault-repo</id>
            <url>https://nexus.hc.to/content/repositories/pub_releases</url>
        </repository>
    </repositories>

    <dependencies>

        <!-- ==================== SERVER API ==================== -->
        <dependency>
            <groupId>org.spigotmc</groupId>
            <artifactId>spigot-api</artifactId>
            <version>1.20.4-R0.1-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>

        <!-- ==================== SLIMEFUN4 ==================== -->
        <!--
            Gunakan fork Gugu jika server kamu pakai versi CN.
            Ganti ke com.github.Slimefun:Slimefun4 jika pakai versi resmi.
        -->
        <dependency>
            <groupId>com.github.SlimefunGuguProject</groupId>
            <artifactId>Slimefun4</artifactId>
            <version>2024.3.1</version>
            <scope>provided</scope>
        </dependency>

        <!-- ==================== SHOPGUIPLUS ==================== -->
        <!--
            ShopGuiPlus tidak ada di repo publik Maven.
            Opsi 1: install manual ke local repo dengan:
              mvn install:install-file -Dfile=ShopGUIPlus.jar \
                -DgroupId=net.brcdev -DartifactId=ShopGUIPlus \
                -Dversion=1.0 -Dpackaging=jar
            Opsi 2: taruh .jar di folder /lib lalu uncomment dependency di bawah.
        -->
        <!--
        <dependency>
            <groupId>net.brcdev</groupId>
            <artifactId>ShopGUIPlus</artifactId>
            <version>1.0</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/lib/ShopGUIPlus.jar</systemPath>
        </dependency>
        -->

        <!-- ==================== VAULT (Economy) ==================== -->
        <dependency>
            <groupId>net.milkbowl.vault</groupId>
            <artifactId>VaultAPI</artifactId>
            <version>1.7</version>
            <scope>provided</scope>
        </dependency>

        <!-- ==================== PLACEHOLDERAPI ==================== -->
        <dependency>
            <groupId>me.clip</groupId>
            <artifactId>placeholderapi</artifactId>
            <version>2.11.5</version>
            <scope>provided</scope>
        </dependency>

        <!-- ==================== GUIZHANLIB (i18n / translation) ==================== -->
        <dependency>
            <groupId>net.guizhanss</groupId>
            <artifactId>GuizhanLibPlugin</artifactId>
            <version>1.8.1</version>
            <scope>provided</scope>
        </dependency>

        <!-- ==================== BSTATS (opsional, analytics) ==================== -->
        <dependency>
            <groupId>org.bstats</groupId>
            <artifactId>bstats-bukkit</artifactId>
            <version>3.0.2</version>
            <scope>compile</scope>
        </dependency>

        <!-- ==================== UTILITIES ==================== -->
        <dependency>
            <groupId>com.google.code.findbugs</groupId>
            <artifactId>jsr305</artifactId>
            <version>3.0.2</version>
            <scope>compile</scope>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.32</version>
            <scope>provided</scope>
        </dependency>

    </dependencies>

    <build>
        <finalName>${project.name}</finalName>
        <defaultGoal>clean package</defaultGoal>
        <sourceDirectory>${basedir}/src/main/java</sourceDirectory>

        <resources>
            <resource>
                <directory>${basedir}/src/main/resources</directory>
                <filtering>true</filtering>
            </resource>
            <resource>
                <directory>${basedir}</directory>
                <includes>
                    <include>LICENSE</include>
                </includes>
            </resource>
        </resources>

        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M5</version>
                <configuration>
                    <junitArtifactName>org.junit.jupiter:junit-jupiter</junitArtifactName>
                    <trimStackTrace>false</trimStackTrace>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.2.4</version>
                <configuration>
                    <minimizeJar>true</minimizeJar>
                    <relocations>
                        <!-- Relocate bStats supaya tidak konflik dengan plugin lain -->
                        <relocation>
                            <pattern>org.bstats</pattern>
                            <shadedPattern>com.github.yourname.autocollect.libs.bstats</shadedPattern>
                        </relocation>
                    </relocations>
                    <filters>
                        <filter>
                            <artifact>*:*</artifact>
                            <excludes>
                                <exclude>META-INF/*</exclude>
                            </excludes>
                        </filter>
                    </filters>
                </configuration>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>
```