package com.example.hemo_conecta

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Associa o NavController à BottomNavigation
        bottomNavigation.setupWithNavController(navController)

        // Intercepta os cliques da BottomNavigation
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.carteirinhaAmbosFragment -> {
                    verificarTipoDoadorENavegar(navController)
                    true
                }
                else -> {
                    // Executa a navegação padrão para as outras abas
                    NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
                }
            }
        }

        // Controla a visibilidade da barra inferior conforme a tela atual
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.inicioFragment,
                R.id.carteirinhaSangueFragment,
                R.id.carteirinhaMedulaFragment,
                R.id.carteirinhaAmbosFragment,
                R.id.naoDoadorFragment,
                R.id.localizacaoFragment,
                R.id.fragmentConteudosEducativos,
                R.id.perfilFragment -> {
                    bottomNavigation.visibility = BottomNavigationView.VISIBLE
                }
                else -> {
                    bottomNavigation.visibility = BottomNavigationView.GONE
                }
            }
        }
    }

    private fun verificarTipoDoadorENavegar(navController: NavController) {
        val uid = auth.currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("usuarios").child(uid)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tipoDoador = snapshot.child("tipoDoador").getValue(String::class.java) ?: "NAO_DOADOR"

                when (tipoDoador) {
                    "SANGUE" -> navController.navigate(R.id.carteirinhaSangueFragment)
                    "MEDULA" -> navController.navigate(R.id.carteirinhaMedulaFragment)
                    "AMBOS" -> navController.navigate(R.id.carteirinhaAmbosFragment)
                    else -> navController.navigate(R.id.naoDoadorFragment)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Erro ao carregar os dados", Toast.LENGTH_SHORT).show()
            }
        })
    }
}