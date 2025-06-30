package com.project.roomdb_replica_ufficiale.fragments.add

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.project.roomdb_replica_ufficiale.R
import com.project.roomdb_replica_ufficiale.data.ingrediente.IngredienteQuantificato


/*
classe adapter per gestire la RecyclerView in AddFragment per l'inserimento degli ingredienti
 */
class AdapterIngrediente(var ingrList: MutableList<IngredienteQuantificato>)
    : Adapter<AdapterIngrediente.MyViewHolder>() {

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.riga_ingrediente, parent, false)

        return MyViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //recupero dell'elemento
        val item = ingrList[position]
        //qui avviene l'aggiornamento
        holder.bind(item)

        /* aggiorna quando la posizione è valida il testo dell'editText ogni volta che il testo
        viene cambiato */
        holder.name.doAfterTextChanged { text ->
            val pos = holder.bindingAdapterPosition
            if(pos != NO_POSITION){
                ingrList[pos].nomeIngrediente = text.toString()
            }
        }
        holder.quantita.doAfterTextChanged { text ->
            val pos = holder.bindingAdapterPosition
            if(pos != NO_POSITION){
                ingrList[pos].quantita = text.toString()
            }
        }

    }

    //Metodo che restituisce la grandezza della lista
    override fun getItemCount(): Int = ingrList.size

    //Classe che gestisce il singolo elemento della lista
    class MyViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        //lista formata dai tre campi name, quantita e deleteBtn
        var name: EditText = itemView.findViewById<EditText>(R.id.nomeIngrediente)
        var quantita: EditText = itemView.findViewById<EditText>(R.id.quantitaIngrediente)
        var deleteBtn: ImageButton = itemView.findViewById<ImageButton>(R.id.deleteButton_ingr)

        //associa per aggiornare i dati visualizzati
        fun bind(item: IngredienteQuantificato) {
            name.setText(item.nomeIngrediente)
            quantita.setText(item.quantita)
        }

        //viene eseguito alla creazione di MyViewHolder
        //imposta il comportamento che ha la lista quando viene premuto il bottone deleteBtn
        init {
            deleteBtn.setOnClickListener {
                listener.onItemClick(bindingAdapterPosition)
            }
        }
    }
}