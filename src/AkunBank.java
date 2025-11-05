/**
 * Class AkunBank ini merepresentasikan akun bank dengan operasi tarik dan setor
 * Di sini saya coba implementasi:
 * - Thread synchronization pakai keyword 'synchronized'
 * - Integrasi dengan database lewat DatabaseManager
 * - Handle race condition yang bisa terjadi kalau banyak thread akses bareng-bareng
 */
public class AkunBank {
    // Pakai 'final' biar ga bisa diubah setelah di-set pertama kali
    private final String nomorRekening;
    private final String namaPemilik;
    private double saldo; // Ini bisa berubah pas ada transaksi

    // Simpan referensi ke DatabaseManager biar bisa update saldo ke database
    private final DatabaseManager dbManager;

    public AkunBank(String nomorRekening, String namaPemilik, double saldo, DatabaseManager dbManager) {
        this.nomorRekening = nomorRekening;
        this.namaPemilik = namaPemilik;
        this.saldo = saldo;
        this.dbManager = dbManager;
    }

    // Getter buat akses data private
    public String getNomorRekening() {
        return nomorRekening;
    }

    public String getNamaPemilik() {
        return namaPemilik;
    }

    public double getSaldo() {
        return saldo;
    }

    /**
     * Method untuk tarik uang dari akun
     * 
     * ini yang penting keyword 'synchronized' digunakan untuk menghindari race condition.
     * Jadi, kalau ada dua thread (misalnya Adi dan Caca) yang mau tarik uang
     * dari akun yang sama di waktu bersamaan, synchronized ini bikin mereka antri.
     * Jadi cuma SATU thread yang boleh masuk ke method ini dalam satu waktu.
     * Yang lain harus NUNGGU sampai yang pertama selesai.
     * 
     * Kalau ga pakai synchronized, bisa kena RACE CONDITION:
     * - Thread 1 cek saldo 1jt, cukup nih buat tarik 800rb
     * - Thread 2 juga cek saldo 1jt (karena thread 1 belum selesai kurangin), cukup buat tarik 700rb
     * - Akhirnya kedua transaksi berhasil padahal harusnya cuma satu yang boleh!
     * 
     * Prosesnya:
     * 1. Cek saldo apakah cukup atau tidak
     * 2. Kasih delay dikit (Thread.sleep) buat simulasi proses bank
     * 3. Kurangin saldo di memory dulu
     * 4. Terus update ke database
     * 5. Kalau gagal update database, rollback (balikin saldo di memory)
     */
    public synchronized void tarik(double jumlah) {
        // Dapetin nama thread yang lagi jalan
        String threadName = Thread.currentThread().getName();

        System.out.println("[" + threadName + "] Mencoba tarik " + jumlah + " dari akun " + nomorRekening
                + ". Saldo saat ini: " + this.saldo);

        // Validasi supaya ga boleh tarik nominal negatif atau 0
        if (jumlah <= 0) {
            System.out.println("-> [" + threadName + "] GAGAL. Nominal harus lebih besar dari 0.");
            return;
        }

        // Cek saldo cukup atau engga
        if (this.saldo >= jumlah) {
            try {
                // delay 100ms buat simulasi proses validasi ke bank
                // ini yang bikin rentan race condition kalau ga pakai synchronized!
                // Karena pas sleep, kalau ga synchronized, thread lain bisa masuk juga
                Thread.sleep(100); 
            } catch (InterruptedException e) {
                // Kalau thread kena interrupt pas lagi sleep
                Thread.currentThread().interrupt();
                System.out.println("-> [" + threadName + "] Thread terganggu, transaksi dibatalkan.");
                return;
            }

            // Simpen dulu saldo sebelumnya buat jaga-jaga kalau nanti gagal
            double saldoSebelum = this.saldo;
            
            // 1. Update saldo di memory (objek Java) dulu
            this.saldo -= jumlah;

            // 2. Terus update ke database
            boolean updateBerhasil = dbManager.updateSaldo(this.nomorRekening, this.saldo);

            if (updateBerhasil) {
                // Berhasil! Saldo di memory dan database udah sama
                System.out.println("-> [" + threadName + "] BERHASIL tarik. Saldo " + saldoSebelum + " -> " + this.saldo);
            } else {
                // Gagal update database, rollback (balikin saldo di memory)
                this.saldo = saldoSebelum;
                System.out.println("-> [" + threadName + "] GAGAL update DB, saldo dikembalikan.");
            }

        } else {
            // Saldo ga cukup
            System.out.println("-> [" + threadName + "] GAGAL. Saldo tidak cukup.");
        }
    }

    /**
     * Method untuk setor uang ke akun
     * 
     * Sama kayak tarik(), method ini juga pakai synchronized
     * Jadi kalau ada beberapa thread yang mau setor bareng-bareng, mereka harus antri
     * 
     * Prosesnya lebih simpel dari tarik():
     * 1. Validasi nominal
     * 2. Tambah saldo di memory
     * 3. Update ke database
     * 4. Kalau gagal, rollback (balikin saldo di memory)
     */
    public synchronized void setor(double jumlah) {
        String threadName = Thread.currentThread().getName();

        System.out.println("[" + threadName + "] Mencoba setor " + jumlah + " ke akun " + nomorRekening
                + ". Saldo saat ini: " + this.saldo);

        // Validasi: nominal harus positif
        if (jumlah <= 0) {
            System.out.println("-> [" + threadName + "] GAGAL. Nominal harus lebih besar dari 0.");
            return;
        }

        // Simpen saldo lama dulu
        double saldoSebelum = this.saldo;
        
        // 1. Update saldo di memory
        this.saldo += jumlah;

        // 2. Update ke database
        boolean updateBerhasil = dbManager.updateSaldo(this.nomorRekening, this.saldo);
        
        if (updateBerhasil) {
            System.out.println("-> [" + threadName + "] BERHASIL setor. Saldo " + saldoSebelum + " -> " + this.saldo);
        } else {
            // Rollback kalau gagal
            this.saldo = saldoSebelum;
            System.out.println("-> [" + threadName + "] GAGAL update DB, saldo dikembalikan.");
        }
    }
}