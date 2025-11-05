import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class DatabaseManager ini saya buat untuk mengatur semua yang berhubungan dengan database.
 * Jadi tugasnya itu:
 * - Bikin koneksi ke database MySQL
 * - Ambil data akun dari database (operasi SELECT)
 * - Update saldo ke database (operasi UPDATE)
 * - Tutup koneksi pas program selesai
 */
public class DatabaseManager {

    // --- Konfigurasi Database ---
    // Ini adalah setting untuk koneksi ke database MySQL (lewat XAMPP)
    // Format: jdbc:mysql://localhost:3306/nama_database
    private static final String DB_URL = "jdbc:mysql://localhost:3306/db_bank"; 
    private static final String DB_USER = "root";  // Username default XAMPP
    private static final String DB_PASS = "adhiet"; //

    // Ini variabel connection yang nanti dipakai untuk komunikasi dengan database
    private Connection connection;

    public DatabaseManager() {
        try {
            // 1. Load driver MySQL dulu
            //    Driver-nya ada di file mysql-connector-java-9.5.0.jar yang ada di folder lib
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 2. Bikin koneksi ke database pakai DriverManager
            //    Kita kasih URL, username, sama password
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("✓ Koneksi database berhasil!");
            
        } catch (ClassNotFoundException e) {
            // Kalau driver-nya ga ketemu (mungkin lupa include file jar-nya)
            System.err.println("❌ Driver MySQL tidak ditemukan: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            // Kalau gagal koneksi (mungkin XAMPP belum nyala atau password salah)
            System.err.println("❌ Gagal koneksi ke database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Method ini untuk ambil data akun dari database berdasarkan nomor rekening
     * Di sini saya pakai PreparedStatement supaya aman dari SQL Injection
     */
    public AkunBank getAkun(String nomorRekening) {
        // Cek dulu, koneksi udah berhasil belum?
        if (connection == null) {
            System.err.println("❌ Koneksi database belum tersedia.");
            return null;
        }
        
        // Query SQL-nya. Tanda tanya (?) itu placeholder yang nanti diisi
        String sql = "SELECT * FROM akun WHERE nomor_rekening = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            // Isi placeholder (?) dengan nomor rekening yang dicari
            // Parameter pertama (1) itu index-nya, mulai dari 1 bukan 0
            stmt.setString(1, nomorRekening);
            
            // Jalankan query SELECT, hasilnya ditaruh di ResultSet
            try (ResultSet rs = stmt.executeQuery()) {
                // rs.next() untuk cek apakah ada data. Kalau ada, pindah ke baris pertama
                if (rs.next()) {
                    // Ambil data dari setiap kolom
                    String noRek = rs.getString("nomor_rekening");
                    String nama = rs.getString("nama_pemilik");
                    double saldo = rs.getDouble("saldo");

                    // Bikin objek AkunBank dari data yang diambil
                    // Parameter 'this' itu DatabaseManager ini sendiri, biar nanti AkunBank bisa update saldo
                    return new AkunBank(noRek, nama, saldo, this);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error saat mengambil akun: " + e.getMessage());
        }
        
        // Kalau sampai sini berarti ga ketemu atau ada error
        return null; 
    }

    /**
     * Method ini untuk update saldo di database
     * Dipanggil dari method tarik() atau setor() di class AkunBank
     * Ini penting banget dalam thread, karena pas method synchronized jalan,
     * kita update database supaya saldo di memory sama dengan saldo di database
     */
    public boolean updateSaldo(String nomorRekening, double saldoBaru) {
        // Cek koneksi dulu
        if (connection == null) {
            System.err.println("❌ Koneksi database belum tersedia. Update dibatalkan.");
            return false;
        }
        
        // Query UPDATE dengan placeholder
        String sql = "UPDATE akun SET saldo = ? WHERE nomor_rekening = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            // Isi placeholder-nya
            stmt.setDouble(1, saldoBaru);        // Placeholder pertama: saldo baru
            stmt.setString(2, nomorRekening);    // Placeholder kedua: nomor rekening
            
            // executeUpdate() ini buat query INSERT, UPDATE, DELETE
            // Bedanya sama executeQuery() yang buat SELECT
            // Return-nya itu jumlah baris yang kena update
            int rowsAffected = stmt.executeUpdate();
            
            // Kalau rowsAffected > 0, berarti ada baris yang keupdate (sukses)
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error saat update saldo: " + e.getMessage());
            return false;
        }
    }

    // Method buat nutup koneksi database
    public void closeConnection() {
        try {
            // Cek apakah koneksi masih ada dan belum ditutup
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close(); // Tutup koneksinya
                System.out.println("✓ Koneksi database ditutup.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error saat menutup koneksi: " + e.getMessage());
        }
    }
}