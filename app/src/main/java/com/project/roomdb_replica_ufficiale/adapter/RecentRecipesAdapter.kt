package com.project.roomdb_replica_ufficiale.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta
import com.project.roomdb_replica_ufficiale.databinding.RecentRecipeItemBinding
import com.project.roomdb_replica_ufficiale.fragments.home.HomeFragmentDirections
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * `RecentRecipesAdapter` è un adattatore per `RecyclerView` che si occupa di visualizzare
 * un elenco di oggetti `Ricetta` in una lista scorrevole.
 * Estende `RecyclerView.Adapter<RecentRecipesAdapter.MyViewHolder>`, il che significa
 * che è responsabile di creare e associare le viste per ogni elemento della lista.
 */
class RecentRecipesAdapter: RecyclerView.Adapter<RecentRecipesAdapter.MyViewHolder>() {
    /**
     * `recipeList` è una variabile privata che contiene l'elenco delle ricette da visualizzare.
     * Viene inizializzata come una lista vuota (`emptyList<Ricetta>()`).
     * Quando i dati vengono aggiornati tramite il metodo `setData`, questa lista viene popolata.
     */
    private var recipeList = emptyList<Ricetta>()
    /**
     * `MyViewHolder` è una classe interna statica che rappresenta un singolo elemento della vista
     * all'interno del `RecyclerView`. Contiene i riferimenti agli elementi UI del layout
     * `RecentRecipeItemBinding` (che è generato automaticamente da View Binding).
     * Il costruttore riceve un'istanza di `RecentRecipeItemBinding` per accedere facilmente
     * alle viste definite nel file XML del layout dell'elemento della lista.
     */
    class MyViewHolder(val binding: RecentRecipeItemBinding): RecyclerView.ViewHolder(binding.root){
        // Non sono necessarie ulteriori inizializzazioni qui, poiché View Binding gestisce i riferimenti.
    }
    /**
     * Questo metodo viene chiamato quando il `RecyclerView` ha bisogno di un nuovo `ViewHolder`
     * per rappresentare un elemento. È responsabile di gonfiare (inflate) il layout dell'elemento
     * della lista e di creare un'istanza di `MyViewHolder` che lo contenga.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Utilizza `LayoutInflater.from(parent.context)` per ottenere un LayoutInflater
        // e gonfia il layout `RecentRecipeItemBinding`.
        val binding = RecentRecipeItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false // `false` significa che la vista non viene attaccata immediatamente al parent.
        )
        return MyViewHolder(binding)
    }
    /**
     * Questo metodo restituisce il numero totale di elementi nella lista dei dati.
     * Il `RecyclerView` lo usa per sapere quanti elementi deve visualizzare.
     */
    override fun getItemCount(): Int {
        return recipeList.size
    }
    /**
     * Questo metodo viene chiamato dal `RecyclerView` per visualizzare i dati in una posizione
     * specifica. È responsabile di aggiornare il contenuto di un `ViewHolder` per riflettere
     * l'elemento di dati nella posizione specificata.
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Ottiene l'oggetto `Ricetta` corrente dalla lista in base alla posizione.
        val currentItem = recipeList[position]
        // Imposta il testo della `recipeName` (nome della ricetta) utilizzando il nome della ricetta corrente.
        holder.binding.recipeName.text = currentItem.nomeRicetta
        // Imposta il testo della `recipeDetails` (dettagli della ricetta) combinando durata e livello.
        holder.binding.recipeDetails.text = "${currentItem.durata} min • ${currentItem.livello}"
        // Imposta il testo della `recipeDate` (data dell'ultima esecuzione) formattando il timestamp.
        holder.binding.recipeDate.text = "Last modification: ${formattaData(currentItem.ultimaModifica)}"

        // Configura un `OnClickListener` per l'intera `cardView` (l'elemento radice della ricetta).
        // Quando la card viene cliccata, l'applicazione naviga verso il `DetailFragment`.
        holder.binding.cardView.setOnClickListener {
            // Crea un'azione di navigazione utilizzando le direzioni generate da Safe Args.
            // Passa l'oggetto `currentItem` (Ricetta) come argomento al `DetailFragment`.
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(currentItem)
            // Trova il `NavController` associato alla vista dell'elemento e naviga.
            holder.itemView.findNavController().navigate(action)
        }


    }
    /**
     * Questo metodo viene utilizzato per aggiornare i dati visualizzati dall'adattatore.
     * Riceve una nuova lista di oggetti `Ricetta` e aggiorna la `recipeList` interna.
     * Successivamente, chiama `notifyDataSetChanged()` per notificare al `RecyclerView`
     * che i dati sono cambiati e che deve ridisegnare la lista.
     */
    fun setData(ricetta: List<Ricetta>){
        this.recipeList = ricetta
        notifyDataSetChanged()
    }
    /**
     * Questo metodo di utilità formatta un timestamp (Long) in una stringa leggibile.
     * Converte il timestamp in un oggetto `LocalDateTime` e lo formatta secondo un pattern specifico.
     */
    fun formattaData(timestamp: Long): String {
        // Definisce il formato desiderato per la data e l'ora.
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm")
        // Ottiene l'ID del fuso orario di sistema predefinito.
        val zona = ZoneId.systemDefault()
        // Converte il timestamp (millisecondi dall'epoca) in un `LocalDateTime`
        // utilizzando il fuso orario specificato.
        val data = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zona)
        // Formatta l'oggetto `LocalDateTime` nella stringa desiderata.
        return data.format(formatter)
    }

}