package com.example.bottomnavigation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bottomnavigation.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // Binding-objekti, joka yhdistää käyttöliittymän elementtejä ja koodia
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ottaa käyttöön reunasta reunaan -näkymän
        enableEdgeToEdge()
        // Sitoo layoutin käyttöliittymään
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Korvaa oletuksena olevan fragmentin Home-fragmentilla
        replaceFragment(Home())

        // Asettaa kuuntelijan alavalikon painikkeille
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                // Valitsee Home-fragmentin
                R.id.home -> replaceFragment(Home())
                // Valitsee Kauppalista-fragmentin
                R.id.kauppalista -> replaceFragment(Kauppalista())
                // Valitsee Muistio-fragmentin
                R.id.muistio -> replaceFragment(Muistio())

                else -> {
                    // Ei tee mitään, jos mikään tunnetuista itemId:istä ei täsmää
                }
            }
            true
        }
    }

    // Korvaa nykyisen fragmentin valitulla fragmentilla
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}
