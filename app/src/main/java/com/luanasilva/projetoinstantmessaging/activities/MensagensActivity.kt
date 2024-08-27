package com.luanasilva.projetoinstantmessaging.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.luanasilva.projetoinstantmessaging.R
import com.luanasilva.projetoinstantmessaging.adapters.MensagensAdapter
import com.luanasilva.projetoinstantmessaging.databinding.ActivityMensagensBinding
import com.luanasilva.projetoinstantmessaging.model.Conversa
import com.luanasilva.projetoinstantmessaging.model.Mensagem
import com.luanasilva.projetoinstantmessaging.model.Usuario
import com.luanasilva.projetoinstantmessaging.utils.Constantes
import com.luanasilva.projetoinstantmessaging.utils.exibirMensagem
import com.squareup.picasso.Picasso

class MensagensActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMensagensBinding.inflate(layoutInflater)
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private lateinit var listenerRegistration: ListenerRegistration
    private var dadosDestinatario: Usuario? = null
    private var dadosUsuarioRemetente: Usuario? = null
    private lateinit var mensagensAdapter: MensagensAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recuperarDadosUsuarios()
        inicializarToolbar()
        inicializarEventosClique()
        inicializarRecyclerView()
        inicializarListeners()
    }

    private fun inicializarRecyclerView() {
        with(binding) {
            mensagensAdapter = MensagensAdapter()
            rvMensagens.adapter = mensagensAdapter
            rvMensagens.layoutManager = LinearLayoutManager(applicationContext)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration.remove()
    }

    private fun inicializarListeners() {
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        val idUsuarioDestinatario = dadosDestinatario?.id
        if(idUsuarioRemetente != null && idUsuarioDestinatario != null ) {

            listenerRegistration = firestore
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
                        mensagensAdapter.adicionarLista(listaMensagens)
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

                val conversaRemetente = Conversa(
                    idUsuarioRemetente, idUsuarioDestinatario,
                    dadosDestinatario!!.foto, dadosDestinatario!!.nome,
                    textoMensagem
                )

                salvarConversaFirestore(conversaRemetente)

                //Salvar para o destinatario
                salvarMensagemFirestore(
                    idUsuarioDestinatario,idUsuarioRemetente,mensagem
                )

                val conversaDestinatario = Conversa(
                    idUsuarioDestinatario, idUsuarioRemetente,
                    dadosUsuarioRemetente!!.foto, dadosUsuarioRemetente!!.nome,
                    textoMensagem
                )
                salvarConversaFirestore(conversaDestinatario)

                binding.editMensagens.setText("")

            }
        }
    }

    private fun salvarConversaFirestore(conversa: Conversa) {

        firestore
            .collection(Constantes.CONVERSAS)
            .document(conversa.idUsuarioRemetente)
            .collection(Constantes.ULTIMAS_CONVERSAS)
            .document(conversa.idUsuarioDestinatario)
            .set(conversa)
            .addOnFailureListener {
                exibirMensagem("Erro ao salvar conversa")
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

    private fun recuperarDadosUsuarios() {
        //Dados usuario logado
        val idUsuarioRemetente =  firebaseAuth.currentUser?.uid
        if(idUsuarioRemetente != null) {
            firestore
                .collection(Constantes.USUARIOS)
                .document(idUsuarioRemetente)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val usuario = documentSnapshot.toObject(Usuario::class.java)
                    if(usuario != null) {
                        dadosUsuarioRemetente = usuario
                    }
                }
        }


        //Recuperando dados destinatario
        val extras = intent.extras
        if(extras != null) {

            //val origem = extras.getString("origem")
            dadosDestinatario = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelable("dadosDestinatario", Usuario::class.java)

            } else {
                extras.getParcelable("dadosDestinatario")
            }

        }
    }
}