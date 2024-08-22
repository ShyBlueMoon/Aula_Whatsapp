package com.luanasilva.projetoinstantmessaging.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.luanasilva.projetoinstantmessaging.databinding.FragmentContatosBinding
import com.luanasilva.projetoinstantmessaging.model.Usuario

class ContatosFragment : Fragment() {


    private lateinit var binding: FragmentContatosBinding

    private lateinit var eventoSnapshot: ListenerRegistration

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentContatosBinding.inflate(
            inflater, container, false
        )

        return binding.root
    }


    override fun onStart() {
        super.onStart()
        adicionarListenerContatos()
    }

    private fun adicionarListenerContatos() {

        val eventoSnapshot = firestore.collection("usuarios")
            .addSnapshotListener { querySnapshot, erro ->

                val listaContatos = mutableListOf<Usuario>()
                val documentos = querySnapshot?.documents

                documentos?.forEach { documentSnapshot ->

                    //Convertendo um documentSnapshot para um Usuário
                    val usuario = documentSnapshot.toObject(Usuario::class.java)
                    if(usuario != null) {

                        val idUsuarioLogado = firebaseAuth.currentUser?.uid
                        if (idUsuarioLogado!= null) {
                            //Exibe toda lista de usuario MENOS o que está logado
                            if (idUsuarioLogado != usuario.id) {
                                listaContatos.add(usuario)
                            }
                        }


                    }


                }

                //Lista de Contatos

            }
    }

    override fun onDestroy() {
        super.onDestroy()
        eventoSnapshot.remove()
    }


}