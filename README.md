# Simulasi Transaksi Bank dengan Thread dan JDBC

Tugas Pemrograman Berorientasi Objek (PBO) - Implementasi Thread dan Database menggunakan Java
## 👤 Identitas

| | |
|---|---|
| **Nama** | Lalu Adittya Pratama Jelindra |
| **NIM** | F1D02310014 |


---

## Deskripsi

Program ini mensimulasikan transaksi bank dengan multiple thread yang mengakses akun yang sama secara bersamaan. Program mendemonstrasikan:

- **Thread** - Multiple thread concurrent
- **JDBC** - Koneksi dan operasi database MySQL
- **Synchronized** - Mengatasi race condition
- **Konsistensi data** - Sinkronisasi antara memory dan database

## Skenario

Dua nasabah (Adi dan Caca) mencoba menarik uang dari akun Budi secara bersamaan:

- Saldo awal: **1.000.000**
- Adi mau tarik: **800.000**
- Caca mau tarik: **700.000**
- Total permintaan: **1.500.000** (lebih dari saldo!)

**Hasil dengan synchronized:**

- Hanya 1 transaksi yang berhasil
- Transaksi lainnya gagal karena saldo tidak cukup
- Tidak terjadi race condition

## Persiapan Database

### 1. Jalankan XAMPP

Pastikan MySQL di XAMPP sudah running

### 2. Buat Database dan Tabel

Buka phpMyAdmin atau MySQL command line, lalu jalankan query berikut:

```sql
-- 1. Buat database
CREATE DATABASE db_bank;

-- 2. Gunakan database
USE db_bank;

-- 3. Buat tabel akun
CREATE TABLE akun (
    nomor_rekening VARCHAR(10) PRIMARY KEY,
    nama_pemilik VARCHAR(100),
    saldo DOUBLE
);

-- 4. Insert data akun Budi
INSERT INTO akun (nomor_rekening, nama_pemilik, saldo)
VALUES ('111', 'Budi', 1000000.0);

-- 5. (Opsional) Insert data akun Ani untuk testing
INSERT INTO akun (nomor_rekening, nama_pemilik, saldo)
VALUES ('222', 'Ani', 500000.0);
```

### 3. Konfigurasi Koneksi Database

Edit file `src/DatabaseManager.java` sesuai dengan setting MySQL:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/db_bank";
private static final String DB_USER = "root";        // Sesuaikan
private static final String DB_PASS = "password";    // Sesuaikan
```

## Folder Structure

```
latihan_thread_dan_jdbc/
├── src/
│   ├── MainApp.java           # Main program
│   ├── DatabaseManager.java   # Koneksi dan operasi database
│   ├── AkunBank.java          # Class akun dengan synchronized
│   └── TugasTransaksi.java    # Runnable task untuk thread
├── lib/
│   └── mysql-connector-j-9.5.0.jar  # JDBC Driver MySQL
├── bin/                       # Compiled classes
└── README.md
```

## Cara Menjalankan

### 1. Compile

```bash
javac -cp "lib/mysql-connector-j-9.5.0.jar;src" -d bin src/*.java
```

### 2. Run

```bash
java -cp "bin;lib/mysql-connector-j-9.5.0.jar" MainApp
```

### 3. Verifikasi Hasil

Cek saldo di database setelah program selesai:

```sql
SELECT * FROM akun WHERE nomor_rekening = '111';
```

Saldo di database harus sama dengan saldo akhir yang ditampilkan program.

## Reset Data (untuk testing ulang)

Jika ingin menjalankan simulasi lagi, reset saldo Budi ke 1 juta:

```sql
UPDATE akun SET saldo = 1000000 WHERE nomor_rekening = '111';
```

## Troubleshooting

### Error: ClassNotFoundException

**Penyebab:** Driver MySQL tidak ditemukan di classpath  
**Solusi:** Pastikan `mysql-connector-j-9.5.0.jar` ada di folder `lib/` dan sudah di-include saat compile/run

### Error: SQLException - Access denied

**Penyebab:** Username/password MySQL salah  
**Solusi:** Cek konfigurasi di `DatabaseManager.java`, sesuaikan dengan setting XAMPP yang dimiliki

### Error: Akun tidak ditemukan

**Penyebab:** Data akun '111' belum ada di database  
**Solusi:** Jalankan query INSERT di bagian Persiapan Database

