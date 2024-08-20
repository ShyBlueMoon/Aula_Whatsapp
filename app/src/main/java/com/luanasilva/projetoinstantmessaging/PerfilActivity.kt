package com.luanasilva.projetoinstantmessaging

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.luanasilva.projetoinstantmessaging.databinding.ActivityPerfilBinding
import com.luanasilva.projetoinstantmessaging.utils.exibirMensagem


class PerfilActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPerfilBinding.inflate(layoutInflater)
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }


    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false
    private val gerenciadorGaleria = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->

        if(uri != null) {

            binding.imagePerfil.setImageURI(uri)
            uploadImagemStorage(uri)

        } else {
            exibirMensagem("Nenhuma imagem selecionada")
        }
    }

    private fun uploadImagemStorage(uri: Uri) {
        //Pasta fotos -> usuarios -idUsuario -> perfil

        val idUsuario = firebaseAuth.currentUser?.uid

        if (idUsuario != null) {
            storage
                .getReference("fotos")
                .child("usuarios")
                .child(idUsuario)
                .child("perfil.jpg")
                .putFile(uri)
                .addOnSuccessListener { task ->

                    exibirMensagem("Sucesso ao fazer upload da imagem")
                    task.metadata
                        ?.reference
                        ?.downloadUrl
                        ?.addOnSuccessListener { url ->

                            val dados = mapOf(
                                "foto" to url.toString()
                            )
                            atualizarDadosPerfil(idUsuario, dados)
                        }

                }.addOnFailureListener{

                    exibirMensagem("Erro ao fazer upload da imagem")

                }
        }
    }

    private fun atualizarDadosPerfil(idUsuario: String, dados: Map<String, String>) {

        firestore.collection("usuarios")
            .document(idUsuario)
            .update(dados)
            .addOnSuccessListener {  exibirMensagem("Sucesso ao atualizar perfil!")}
            .addOnFailureListener{exibirMensagem("Erro ao atualizar perfil")}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        inicializarToolbar()
        solicitarPermissoes()
        inicializarEventosClique()
    }

    private fun inicializarEventosClique() {
        binding.fabSelecionar.setOnClickListener {
            if(temPermissaoGaleria) {
                gerenciadorGaleria.launch("image/*")
            } else {
                exibirMensagem("Não tem permissão para acessar a galeria!")
                solicitarPermissoes()
            }
        }

        binding.btnAtualizarPerfil.setOnClickListener {

            val nomeUsuario = binding.editNomePerfil.text.toString()
            if(nomeUsuario.isNotEmpty()) {

                val idUsuario = firebaseAuth.currentUser?.uid
                if(idUsuario != null) {
                    val dados = mapOf(
                        "nome" to nomeUsuario
                    )
                    atualizarDadosPerfil(idUsuario, dados)

                }


            } else {
                exibirMensagem("Preencha o nome para atualizar")
            }

        }

    }

    private fun solicitarPermissoes() {
        //Verificar se usuario ja tem permissao
        temPermissaoCamera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        temPermissaoGaleria = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED


        //Lista de permissões negadas
        val listaPermissoesNegadas =  mutableListOf<String>()

        if(!temPermissaoCamera)
            listaPermissoesNegadas.add(Manifest.permission.CAMERA)
        if(!temPermissaoGaleria)
            listaPermissoesNegadas.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        if (listaPermissoesNegadas.isNotEmpty()) {
            //Solicitar múltiplas permissões
            val gerenciadorPermissoes = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissoes ->

                temPermissaoCamera=permissoes[Manifest.permission.CAMERA]
                    ?: temPermissaoCamera

                temPermissaoGaleria=permissoes[Manifest.permission.READ_EXTERNAL_STORAGE]
                    ?: temPermissaoGaleria


            }
            gerenciadorPermissoes.launch(listaPermissoesNegadas.toTypedArray())
        }



    }







    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbarPerfil.tbPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Editar Perfil"
            //Configura o botão de voltar para a activity parent que consta no Manifest
            setDisplayHomeAsUpEnabled(true)
        }
    }
}