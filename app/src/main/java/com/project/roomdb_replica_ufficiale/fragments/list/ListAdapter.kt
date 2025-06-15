package com.project.roomdb_replica_ufficiale.fragments.list

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta
import com.project.roomdb_replica_ufficiale.databinding.CustomRowBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ListAdapter: RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    private var recipeList = emptyList<Ricetta>()

    // Interfaccia per le azioni
    interface OnItemActionListener {
        fun onDeleteClicked(recipe: Ricetta)
    }

    private var listener: OnItemActionListener? = null

    fun setOnItemActionListener(listener: OnItemActionListener) {
        this.listener = listener
    }

    class MyViewHolder(val binding: CustomRowBinding): RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = CustomRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = recipeList[position]
        holder.binding.recipeName.text = currentItem.nomeRicetta
        holder.binding.recipeDetails.text = currentItem.durata.toString()
        holder.binding.recipeDate.text = formattaData(currentItem.ultimaEsecuzione)

        holder.binding.cardView.setOnClickListener{
            //val action = ListFragmentDirections.actionListFragmentToUpdateFragment(currentItem)
            val action = ListFragmentDirections.actionListFragmentToDetailFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }

        holder.binding.updateBtn.setOnClickListener{
            val action = ListFragmentDirections.actionListFragmentToUpdateFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }
        holder.binding.newIconButton.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToSvolgiRicettaFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }

        holder.binding.deleteBtnRow.setOnClickListener {
            listener?.onDeleteClicked(currentItem)
        }

    }

    fun setData(user: List<Ricetta>){
        this.recipeList = user
        notifyDataSetChanged()
    }

    fun formattaData(timestamp: Long): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm")
        val zona = ZoneId.systemDefault() // oppure ZoneId.of("Europe/Rome")
        val data = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zona)
        return data.format(formatter)
    }


}