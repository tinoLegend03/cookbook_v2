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

/**
 * Adapter per la RecyclerView che mostra l’elenco delle ricette.
 * Espone un listener per delete e altre azioni sul singolo elemento.
 */
class ListAdapter: RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    /* Lista dati corrente visualizzata */
    private var recipeList = emptyList<Ricetta>()

    /* ---------- Callback esterna per azioni su un elemento ------------------ */

    interface OnItemActionListener {
        fun onDeleteClicked(recipe: Ricetta)
    }

    private var listener: OnItemActionListener? = null

    fun setOnItemActionListener(listener: OnItemActionListener) {
        this.listener = listener
    }


    /* ---------- ViewHolder ------------------------------------------------ */

    /** Usa il binding generato da viewBinding
     * custom_row.xml -> CustomRowBinding).
     */
    class MyViewHolder(val binding: CustomRowBinding): RecyclerView.ViewHolder(binding.root){

    }

    /* ---------- creazione ViewHolder ----------------------------------------- */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = CustomRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    /* ---------- Ritorna il numero di ricette salvate ------------------ */
    override fun getItemCount(): Int {
        return recipeList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = recipeList[position]

        /* ------ Popola le view della card ------  */
        holder.binding.recipeName.text = currentItem.nomeRicetta
        holder.binding.recipeDetails.text = "${currentItem.durata} min • ${currentItem.livello}"
        holder.binding.recipeDate.text = formattaData(currentItem.ultimaEsecuzione)

        /* ------ Gestione del click sulla card che rimanda a Details ------ */
        holder.binding.cardView.setOnClickListener{
            val action = ListFragmentDirections.actionListFragmentToDetailFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }

        /*  ------ Pulsante modifica ------ */
        holder.binding.updateBtn.setOnClickListener{
            val action = ListFragmentDirections.actionListFragmentToUpdateFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }

        /*  ------ Pulsante “step-by-step” ------ */
        holder.binding.newIconButton.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToSvolgiRicettaFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }


        /* ------ Pulsante delete  ------ */
        holder.binding.deleteBtnRow.setOnClickListener {
            listener?.onDeleteClicked(currentItem)
        }

    }

    /* ----------- Aggiorna dataset e rinfresca la lista  -------------*/
    fun setData(user: List<Ricetta>){
        this.recipeList = user
        notifyDataSetChanged()
    }

    /* ----------- Converte timestamp in stringa leggibile  --------------*/
    fun formattaData(timestamp: Long): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm")
        val zona = ZoneId.systemDefault() // oppure ZoneId.of("Europe/Rome")
        val data = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zona)
        return data.format(formatter)
    }


}