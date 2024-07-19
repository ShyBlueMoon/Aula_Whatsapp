package com.luanasilva.projetoinstantmessaging

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.luanasilva.projetoinstantmessaging.databinding.ActivityLoginBinding
import com.luanasilva.projetoinstantmessaging.utils.exibirMensagem

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

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

        inicializarEventos()
        firebaseAuth.signOut()
    }

    override fun onStart() {
        super.onStart()
        verificarUsuarioLogado()
    }

    private fun verificarUsuarioLogado() {
        val usuarioAtual = firebaseAuth.currentUser
        if (usuarioAtual != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun inicializarEventos() {
        binding.textCadastro.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }

        binding.btnLogar.setOnClickListener {
            if(validarCampos()) {
                logarUsuario()
            }
        }
    }

    private fun logarUsuario() {
        firebaseAuth.signInWithEmailAndPassword(
            email, senha
        ).addOnSuccessListener {
            exibirMensagem("Logado com sucesso")
            startActivity(Intent(this, MainActivity::class.java))
        }.addOnFailureListener { erro ->

            try {
                throw erro
            } catch (erroUsuarioInvalido: FirebaseAuthInvalidUserException) {
                erroUsuarioInvalido.printStackTrace()
                exibirMensagem("E-mail não cadastrado")
            }catch (erroCredenciaisInvalidas: FirebaseAuthInvalidCredentialsException) {
                erroCredenciaisInvalidas.printStackTrace()
                exibirMensagem("E-mail ou senha incorretos!")
            }
        }
    }

    private fun validarCampos(): Boolean {
        email = binding.editLoginEmail.text.toString()
        senha = binding.editLoginSenha.text.toString()

        if(email.isNotEmpty()) {
            binding.textInputLayoutLoginEmail.error = null
            if (senha.isNotEmpty()) {
                binding.textInputLayoutLoginEmail.error = null
                return true
            } else {
                binding.textInputLayoutLoginSenha.error = "Preencha a senha"
                return false
            }
        } else {
            binding.textInputLayoutLoginEmail.error = "Preencha o e-mail"
            return false
        }
    }

}


