package com.luanasilva.projetoinstantmessaging.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.luanasilva.projetoinstantmessaging.fragments.ContatosFragment
import com.luanasilva.projetoinstantmessaging.fragments.ConversasFragment

class ViewPageAdapter(

    val abas: List<String>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    //Quantidade de abas que vc tem
    override fun getItemCount(): Int {
        return abas.size // 0 - Conversas, 1 - Contatos
    }

    override fun createFragment(position: Int): Fragment {
        when(position) {
            1 -> return ContatosFragment()

        }
        //retornar fragment padrão
        return ConversasFragment()
    }
}