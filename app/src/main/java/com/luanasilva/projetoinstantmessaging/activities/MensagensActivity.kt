package com.luanasilva.projetoinstantmessaging.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.luanasilva.projetoinstantmessaging.R
import com.luanasilva.projetoinstantmessaging.databinding.ActivityMensagensBinding
import com.luanasilva.projetoinstantmessaging.model.Mensagem
import com.luanasilva.projetoinstantmessaging.model.Usuario
import com.luanasilva.projetoinstantmessaging.utils.Constantes
import com.luanasilva.projetoinstantmessaging.utils.exibirMensagem
import com.squareup.picasso.Picasso

class MensagensActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMensagensBinding.inflate(layoutInflater)
    }
    private var dadosDestinatario: Usuario? = null

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private lateinit var listenerRegistration: ListenerRegistration



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recuperarDadosUsuarioDestinatario()
        inicializarToolbar()
        inicializarEventosClique()
        inicializarListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration.remove()
    }

    private fun inicializarListeners() {
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        val idUsuarioDestinatario = dadosDestinatario?.id
        if(idUsuarioRemetente != null && idUsuarioDestinatario != null ) {

            val listenerRegistration = firestore
                .collection(Constantes.MENSAGENS)
                .document(idUsuarioRemetente)
                .collection(idUsuarioDestinatario)
                .orderBy("data", Query.Direction.ASCENDING)
                .addSnapshotListener { querySnapshot, erro ->

                    if(erro != null) {
                        exibirMensagem("Erro ao recuperar mensagens")
                    }

                    val listaMensagens = mutableListOf<Mensagem>()
                    val documentos = querySnapshot?.documents

                    documentos?.forEach { documentSnapshot ->
                        val mensagem = documentSnapshot.toObject(Mensagem::class.java)
                        if(mensagem != null) {
                            listaMensagens.add(mensagem)
                            Log.i("exibiçcao_mensagem",mensagem.mensagem)
                        }
                    }

                    //Lista
                    if(listaMensagens.isNotEmpty()) {
                        //Carregar dados adapter
                    }

                }

        }
    }

    private fun inicializarEventosClique() {
        binding.fabEnviar.setOnClickListener {
            val mensagem = binding.editMensagens.text.toString()
            salvarMensagem(mensagem)
        }
    }

    private fun salvarMensagem(textoMensagem: String) {
        if(textoMensagem.isNotEmpty()) {
            val idUsuarioRemetente = firebaseAuth.currentUser?.uid
            val idUsuarioDestinatario = dadosDestinatario?.id
            if(idUsuarioRemetente != null && idUsuarioDestinatario != null ) {
                val mensagem = Mensagem(
                    idUsuarioRemetente,textoMensagem
                )
                //Salvar para o remetente
                salvarMensagemFirestore(
                    idUsuarioRemetente,idUsuarioDestinatario,mensagem
                )
                //Salvar para o destinatario
                salvarMensagemFirestore(
                    idUsuarioDestinatario,idUsuarioRemetente,mensagem
                )

                binding.editMensagens.setText("")

            }
        }
    }

    private fun salvarMensagemFirestore(
        idUsuarioRemetente: String, idUsuarioDestinatario: String, mensagem: Mensagem
    ) {
        firestore.collection(Constantes.MENSAGENS)
            .document(idUsuarioRemetente)
            .collection(idUsuarioDestinatario)
            .add(mensagem)
            .addOnFailureListener {
                exibirMensagem("Erro ao enviar mensagem")
            }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.tbMensagens
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            if(dadosDestinatario != null) {
                binding.textNome.text = dadosDestinatario!!.nome
                Picasso.get()
                    .load(dadosDestinatario!!.foto)
                    .into(binding.imageFotoPerfil)
            }
            //Configura o botão de voltar para a activity parent que consta no Manifest
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun recuperarDadosUsuarioDestinatario() {
        val extras = intent.extras
        if(extras != null) {

            val origem = extras.getString("origem")
            if (origem == Constantes.ORIGEM_CONTATO) {

                dadosDestinatario = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    extras.getParcelable("dadosDestinatario", Usuario::class.java)

                } else {
                    extras.getParcelable("dadosDestinatario")
                }


            } else if (origem == Constantes.ORIGEM_CONVERSA) {

            }

        }
    }
}