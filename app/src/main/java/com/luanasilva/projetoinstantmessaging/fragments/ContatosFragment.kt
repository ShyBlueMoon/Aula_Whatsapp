package com.luanasilva.projetoinstantmessaging.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.luanasilva.projetoinstantmessaging.activities.MensagensActivity
import com.luanasilva.projetoinstantmessaging.adapters.ContatosAdapter
import com.luanasilva.projetoinstantmessaging.databinding.FragmentContatosBinding
import com.luanasilva.projetoinstantmessaging.model.Usuario
import com.luanasilva.projetoinstantmessaging.utils.Constantes

class ContatosFragment : Fragment() {


    private lateinit var binding: FragmentContatosBinding
    private lateinit var eventoSnapshot: ListenerRegistration
    private lateinit var contatosAdapter:ContatosAdapter


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

        contatosAdapter = ContatosAdapter{ usuario ->
            val intent = Intent(context, MensagensActivity::class.java)
            intent.putExtra("dadosDestinatario", usuario)
            intent.putExtra("origem", Constantes.ORIGEM_CONTATO)
            startActivity(intent)
        }
        binding.rvContatos.adapter = contatosAdapter
        binding.rvContatos.layoutManager = LinearLayoutManager(context)
        binding.rvContatos.addItemDecoration(
            DividerItemDecoration(
                context, LinearLayoutManager.VERTICAL
            )
        )


        return binding.root
    }


    override fun onStart() {
        super.onStart()
        adicionarListenerContatos()
    }

    private fun adicionarListenerContatos() {

        eventoSnapshot = firestore.collection(Constantes.USUARIOS)
            .addSnapshotListener { querySnapshot, erro ->

                val listaContatos = mutableListOf<Usuario>()
                val documentos = querySnapshot?.documents

                documentos?.forEach { documentSnapshot ->
                    val idUsuarioLogado = firebaseAuth.currentUser?.uid
                    //Convertendo um documentSnapshot para um Usuário
                    val usuario = documentSnapshot.toObject(Usuario::class.java)

                    if(usuario != null && idUsuarioLogado != null) {
                        if (idUsuarioLogado != usuario.id) {
                            listaContatos.add(usuario)
                        }

                    }


                }

                //Lista de Contatos(atualizar o RV)
                if(listaContatos.isNotEmpty()) {
                    contatosAdapter.adicionarLista(listaContatos)
                }

            }
    }

    override fun onDestroy() {
        super.onDestroy()
        eventoSnapshot.remove()
    }


}