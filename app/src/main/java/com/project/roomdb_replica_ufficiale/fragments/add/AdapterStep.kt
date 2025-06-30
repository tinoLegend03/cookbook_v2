package com.project.roomdb_replica_ufficiale.fragments.add

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.project.roomdb_replica_ufficiale.R
import com.project.roomdb_replica_ufficiale.data.istruzione.Istruzione

/*
classe adapter per gestire la RecyclerView in AddFragment per l'inserimento delle istruzioni (step)
 */

class AdapterStep(var stepList: MutableList<String>)
    : RecyclerView.Adapter<AdapterStep.MyViewHolder>(){

    /*
    creazione e gestione di un listener del tipo inItemClick, per gestire l'azione di eliminazione
    elemento durante l'applicazione.
    */
    interface OnItemClickListener { fun onItemClick(pos: Int) }
    lateinit var listener: OnItemClickListener

    fun setOnItemClickListener(clickListener: OnItemClickListener){
        listener = clickListener
    }

    //Metodo per creare una nuova viewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //inserisce nella riga il layout riga_ingrediente
        val view = LayoutInflater.from(parent.context).inflate(R.layout.riga_istruzione, parent, false)

        return MyViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //recupero dell'elemento
        val currentItem = stepList[position]
        //qui avviene l'aggiornamento
        holder.bind(currentItem)

        /* aggiorna quando la posizione è valida il testo dell'editText ogni volta che il testo
        viene cambiato */
        holder.description.doAfterTextChanged { text ->
            val pos = holder.bindingAdapterPosition
            if(pos != NO_POSITION){
                stepList[pos] = text.toString()
            }
        }

    }

    override fun getItemCount(): Int = stepList.size

    //Classe che gestisce il singolo elemento della lista
    class MyViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        //lista formata dai tre campi description, numberStep e removeButton
        var description = itemView.findViewById<EditText>(R.id.description_step)
        var numberStep = itemView.findViewById<TextView>(R.id.number_step)
        val removeButton = itemView.findViewById<ImageButton>(R.id.trash_button_steps)

        fun bind(itemViewHolder: String){
            description.setText(itemViewHolder.toString())
            numberStep.text = (bindingAdapterPosition + 1).toString() //impostazione dell'indice
        }

        //viene eseguito alla creazione di MyViewHolder
        //imposta il comportamento che ha la lista quando viene premuto il bottone deleteBtn
        init {
            removeButton.setOnClickListener {
                listener.onItemClick(bindingAdapterPosition)
            }
        }
    }

}