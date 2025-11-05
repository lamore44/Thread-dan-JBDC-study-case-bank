/**
 * TUGAS PBO - Simulasi Transaksi Bank dengan Thread dan Database
 * 
 * Skenario:
 * Dua nasabah (Adi dan Caca) tarik uang dari akun yang sama secara bersamaan.
 * Saldo awal: 1.000.000
 * Adi mau tarik: 800.000
 * Caca mau tarik: 700.000
 * Total: 1.500.000 (lebih dari saldo!)
 * 
 * Dengan synchronized: Hanya 1 yang berhasil, yang lain gagal (saldo ga cukup)
 * Tanpa synchronized: Bisa race condition (kedua berhasil padahal ga cukup)
 */
public class MainApp {

    public static void main(String[] args) {
        System.out.println("=== SIMULASI TRANSAKSI BANK ===\n");

        // 1. Bikin koneksi ke database
        DatabaseManager dbManager = new DatabaseManager();

        // 2. Ambil data akun dari database
        AkunBank akunBudi = dbManager.getAkun("111");
        
        if (akunBudi == null) {
            System.out.println("Akun tidak ditemukan!");
            dbManager.closeConnection();
            return;
        }

        System.out.println("Akun: " + akunBudi.getNamaPemilik());
        System.out.println("Saldo Awal: " + akunBudi.getSaldo() + "\n");

        // 3. Bikin tugas transaksi untuk 2 thread
        TugasTransaksi tugasAdi = new TugasTransaksi(akunBudi, 800000, "tarik");
        TugasTransaksi tugasCaca = new TugasTransaksi(akunBudi, 700000, "tarik");

        // 4. Bikin thread
        Thread threadAdi = new Thread(tugasAdi, "Adi");
        Thread threadCaca = new Thread(tugasCaca, "Caca");

        // 5. Jalankan thread (bersamaan!)
        threadAdi.start();
        threadCaca.start();

        // 6. Tunggu sampai kedua thread selesai
        try {
            threadAdi.join();
            threadCaca.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 7. Tampilkan hasil
        System.out.println("\n=== HASIL ===");
        System.out.println("Saldo Akhir: " + akunBudi.getSaldo());
        
        // 8. Tutup koneksi
        dbManager.closeConnection();
    }
}