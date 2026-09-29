package com.example.hemo_conecta

import android.R.attr.onClick
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.databinding.FragmentInicioBinding
import com.example.hemo_conecta.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth

class PerfilFragment : Fragment() {

    private var  _binding: FragmentPerfilBinding? = null

    private val binding get()  = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentPerfilBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view:View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        initListeners()
    }

    private fun initListeners(){
        binding.btnLogout.setOnClickListener {
                auth.signOut()
                findNavController().navigate(R.id.action_inicioFragment_to_authentication)
            }
        }
    }



