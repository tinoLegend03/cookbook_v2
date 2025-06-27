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

class AdapterStep(var stepList: MutableList<String>)
    : RecyclerView.Adapter<AdapterStep.MyViewHolder>(){

    interface OnItemClickListener { fun onItemClick(pos: Int) }

    lateinit var listener: OnItemClickListener

    fun setOnItemClickListener(clickListener: OnItemClickListener){
        listener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.riga_istruzione, parent, false)

        return MyViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = stepList[position]
        holder.bind(currentItem)

        holder.description.doAfterTextChanged { text ->
            val pos = holder.bindingAdapterPosition
            if(pos != NO_POSITION){
                stepList[pos] = text.toString()
            }
        }

    }

    override fun getItemCount(): Int = stepList.size

    class MyViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        var description = itemView.findViewById<EditText>(R.id.description_step)
        var numberStep = itemView.findViewById<TextView>(R.id.number_step)
        val removeButton = itemView.findViewById<ImageButton>(R.id.trash_button_steps)

        fun bind(itemViewHolder: String){
            description.setText(itemViewHolder.toString())
            numberStep.text = (bindingAdapterPosition + 1).toString()
        }

        init {
            removeButton.setOnClickListener {
                listener.onItemClick(bindingAdapterPosition)
            }
        }
    }

}