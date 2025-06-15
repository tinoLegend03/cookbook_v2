package com.project.roomdb_replica_ufficiale.fragments.add

import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.project.roomdb_replica_ufficiale.R
import com.project.roomdb_replica_ufficiale.data.ingrediente.IngredienteQuantificato
import kotlin.collections.get

class AdapterIngrediente(var ingrList: MutableList<IngredienteQuantificato>)
    : RecyclerView.Adapter<AdapterIngrediente.MyViewHolder>() {

    interface OnItemClickListener { fun onItemClick(pos: Int) }

    lateinit var listener: OnItemClickListener

    fun setOnItemClickListener(clickListener: OnItemClickListener){
        listener = clickListener
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.riga_ingrediente, parent, false)

        return MyViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = ingrList[position]
        holder.bind(item)

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

    override fun getItemCount(): Int {
        return ingrList.size
    }

    class MyViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        var name: EditText = itemView.findViewById<EditText>(R.id.nomeIngrediente)
        var quantita: EditText = itemView.findViewById<EditText>(R.id.quantitaIngrediente)
        var deleteBtn: ImageButton = itemView.findViewById<ImageButton>(R.id.deleteButton_ingr)

        fun bind(item: IngredienteQuantificato) {
            name.setText(item.nomeIngrediente)
            quantita.setText(item.quantita)
        }

        init {
            deleteBtn.setOnClickListener {
                Log.d("DELETE BTN", "delete button pressed")
                listener.onItemClick(bindingAdapterPosition)
            }
        }
    }
}