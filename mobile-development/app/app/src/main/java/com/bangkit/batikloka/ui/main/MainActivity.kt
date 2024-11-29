package com.bangkit.batikloka.ui.main
//by rajahafiz//
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // Menampilkan dialog berdasarkan tindakan yang dilakukan
//        val action = intent.getStringExtra("action")
//        when (action) {
//            "login" -> showCustomAlertDialog("Welcome back to BatikLoka!")
//            "register" -> showCustomAlertDialog("Welcome to BatikLoka! Thank you for registering")
//            else -> showCustomAlertDialog("Welcome to BatikLoka")
//        }
//
//        // Setup BottomNavigationView
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
//        bottomNavigationView.setOnItemSelectedListener { menuItem ->
//            val fragment: Fragment = when (menuItem.itemId) {
//                R.id.home -> HomeFragment()
//                R.id.news -> NewsFragment()
//                R.id.catalog -> CatalogFragment()
//                R.id.user -> UserFragment()
//                else -> HomeFragment()
//            }
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, fragment)
//                .commit()
//            true
//        }
//
//        // Set default fragment
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, HomeFragment())
//                .commit()
//        }
//    }
//
//    private fun showCustomAlertDialog(title: String) {
//        // Inflate layout kustom
//        val dialogView = layoutInflater.inflate(R.layout.dialog_checkmark, null)
//        val titleTextView = dialogView.findViewById<TextView>(R.id.dialog_title)
//        titleTextView.text = title
//
//        val dialog = AlertDialog.Builder(this)
//            .setView(dialogView)
//            .setCancelable(true) // Memungkinkan dialog ditutup dengan mengklik di luar
//            .create()
//
//        dialog.setOnShowListener {
//            // Mengatur latar belakang dialog dengan drawable kustom
//            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)
//
//            // Menangani klik di luar dialog untuk menutup dialog
//            dialog.setCanceledOnTouchOutside(true)
//        }
//
//        dialog.show()
//
//        // Menambahkan delay sebelum dialog ditutup
//        Handler(Looper.getMainLooper()).postDelayed({
//            if (dialog.isShowing) {
//                dialog.dismiss() // Menutup dialog setelah delay
//            }
//        }, 3000)
//    }
}
