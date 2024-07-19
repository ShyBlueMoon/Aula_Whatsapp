package com.luanasilva.projetoinstantmessaging

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.luanasilva.projetoinstantmessaging.databinding.ActivityCadastroBinding
import com.luanasilva.projetoinstantmessaging.model.Usuario
import com.luanasilva.projetoinstantmessaging.utils.exibirMensagem

class CadastroActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCadastroBinding.inflate(layoutInflater)
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firestore by lazy {
            FirebaseFirestore.getInstance()
        }

    private lateinit var nome: String
    private lateinit var email: String
    private lateinit var senha: String

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
        inicializarEventosClique()
    }

    private fun inicializarEventosClique() {
        binding.btnCadastrar.setOnClickListener {
            if (validarCampos()) {
                //Criar usuários
                cadastrarUsuario(nome, email, senha)
            }
        }

    }

    private fun cadastrarUsuario(nome: String, email: String, senha: String) {

        firebaseAuth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener() { resultado ->
                if (resultado.isSuccessful) {
                    //Salvar dados no Firestore Database

                    val idUsuario =  resultado.result.user?.uid
                    if(idUsuario != null) {
                        val usuario = Usuario(
                            idUsuario, nome, email
                        )
                        salvarUsuarioFirestore(usuario)
                    }



                }
            }.addOnFailureListener { erro ->
                try {
                    throw erro
                } catch (erroSenhaFraca: FirebaseAuthWeakPasswordException) {
                    erroSenhaFraca.printStackTrace()
                    exibirMensagem("Senha fraca. Digite outra senha")
                } catch (erroUsuarioExistente: FirebaseAuthUserCollisionException) {
                    erroUsuarioExistente.printStackTrace()
                    exibirMensagem("E-mail ja pertence a outro usuario")
                } catch (erroCredenciaisInvalidas: FirebaseAuthInvalidCredentialsException) {
                    erroCredenciaisInvalidas.printStackTrace()
                    exibirMensagem("E-mail inválido, digite outro e-mail")

                }
            }




    }

    private fun salvarUsuarioFirestore(usuario: Usuario) {
        firestore
            .collection("usuarios")
            .document(usuario.id)
            .set(usuario)
            .addOnSuccessListener {
                exibirMensagem("Sucesso ao fazer seu cadastro")
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }.addOnFailureListener {
                exibirMensagem("Falha ao fazer seu cadastro")
            }
    }

    private fun validarCampos(): Boolean {

        nome = binding.editNome.text.toString()
        email = binding.editEmail.text.toString()
        senha = binding.editSenha.text.toString()



        if (nome.isNotEmpty()) {
            binding.textInputLayoutNome.error = null

            if (email.isNotEmpty()) {
                binding.textInputLayoutEmail.error = null

                if (senha.isNotEmpty()) {
                    binding.textInputLayoutSenha.error = null
                    return true
                } else {
                    binding.textInputLayoutSenha.error = "Preencha o sua senha!"
                    return false
                }

            } else {
                binding.textInputLayoutEmail.error = "Preencha o seu e-mail!"
                return false
            }

        } else {
            binding.textInputLayoutNome.error = "Preencha o seu nome!"
            return false
        }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbar.tbPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Faça seu cadastro"
            //Configura o botão de voltar para a activity parent que consta no Manifest
            setDisplayHomeAsUpEnabled(true)
        }
    }
}