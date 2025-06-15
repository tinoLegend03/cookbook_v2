package com.project.roomdb_replica_ufficiale.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta
import com.project.roomdb_replica_ufficiale.databinding.RecentRecipeItemBinding
import com.project.roomdb_replica_ufficiale.fragments.home.HomeFragmentDirections
import com.project.roomdb_replica_ufficiale.fragments.list.ListFragmentDirections
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RecentRecipesAdapter: RecyclerView.Adapter<RecentRecipesAdapter.MyViewHolder>() {

    private var recipeList = emptyList<Ricetta>()



    class MyViewHolder(val binding: RecentRecipeItemBinding): RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecentRecipeItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = recipeList[position]
        holder.binding.recipeName.text = currentItem.nomeRicetta
        holder.binding.recipeDetails.text = "${currentItem.durata} min • ${currentItem.livello}"
        holder.binding.recipeDate.text = "Ultima volta: ${formattaData(currentItem.ultimaEsecuzione)}"

        // Click su tutta la card → vai al dettaglio
        holder.binding.cardView.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }


    }

    fun setData(ricetta: List<Ricetta>){
        this.recipeList = ricetta
        notifyDataSetChanged()
    }

    fun formattaData(timestamp: Long): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm")
        val zona = ZoneId.systemDefault() // oppure ZoneId.of("Europe/Rome")
        val data = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zona)
        return data.format(formatter)
    }

}