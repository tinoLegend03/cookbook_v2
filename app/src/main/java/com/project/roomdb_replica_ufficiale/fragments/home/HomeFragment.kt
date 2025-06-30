package com.project.roomdb_replica_ufficiale.fragments.home

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.roomdb_replica_ufficiale.R
import com.project.roomdb_replica_ufficiale.data.ricetta.RicettaViewModel
import com.project.roomdb_replica_ufficiale.databinding.FragmentHomeBinding
import androidx.lifecycle.Observer
import com.project.roomdb_replica_ufficiale.adapter.RecentRecipesAdapter

/**
 * `HomeFragment` è il Fragment principale dell'applicazione, che funge da dashboard.
 * Permette all'utente di navigare verso altre sezioni (aggiungi ricetta, le mie ricette)
 * e visualizza un elenco delle ricette modificate più di recente.
 * Gestisce anche la richiesta di permessi per le notifiche su Android 13 (API 33) e superiori.
 */
class HomeFragment : Fragment() {

    // Dichiarazione del ViewModel per interagire con i dati delle ricette.
    private lateinit var recipeViewModel: RicettaViewModel
    // `_binding` è una variabile nullable che conterrà l'istanza di View Binding per questo Fragment.
    private var _binding: FragmentHomeBinding? = null
    // `binding` è una proprietà di sola lettura che fornisce un accesso non-nullable a `_binding`.
    // L'operatore `!!` (not-null asserted call) viene usato qui, il che significa che si assume
    // che `_binding` non sarà mai nullo quando si accede a `binding`.
    private val binding get() = _binding!!

    /**
    * Questo metodo viene chiamato per creare e restituire la gerarchia di viste associata al Fragment.
    * È qui che il layout del Fragment viene gonfiato e le viste vengono inizializzate.
    */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Gonfia il layout per questo Fragment utilizzando View Binding.
        // `FragmentHomeBinding.inflate` crea un'istanza della classe di binding
        // che contiene i riferimenti a tutte le viste nel layout `fragment_home.xml`.
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        // Configura un `OnClickListener` per la card `cardAddRecipe`
        // Quando viene cliccata, naviga al `AddFragment` (per aggiungere una nuova ricetta).
        binding.cardAddRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addFragment)
        }
        // Configura un `OnClickListener` per la card `cardMyRecipes`.
        // Quando cliccata, naviga al `ListFragment` (per visualizzare tutte le ricette).
        binding.cardMyRecipes.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_listFragment)
        }

        // --- Configurazione della RecyclerView per le ricette recenti ---
        // Crea un'istanza dell'adattatore `RecentRecipesAdapter`.
        val adapter = RecentRecipesAdapter()
        val recyclerView = _binding!!.recyclerViewRecentlyMade
        // Ottiene un riferimento alla RecyclerView dal binding.
        recyclerView.adapter = adapter
        // Imposta l'adattatore per la RecyclerView.
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Imposta il LayoutManager per la RecyclerView e visualizza
        // gli elementi in una lista verticale scorrevole.
        recipeViewModel = ViewModelProvider(this).get(RicettaViewModel::class.java)
        // Osserva il LiveData `ultime10Ricette` dal ViewModel.
        // Ogni volta che la lista delle ultime 10 ricette cambia nel database,
        // l'Observer viene notificato e il metodo `setData` dell'adattatore viene chiamato
        // per aggiornare la RecyclerView.
        recipeViewModel.ultime10Ricette.observe(viewLifecycleOwner, Observer { ricetta ->
            adapter.setData(ricetta)
        })

        // --- Gestione dei permessi per le notifiche (solo da Android 13/API 33 in su) ---
        // Controlla la versione dell'SDK Android del dispositivo.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Se la versione è Android 13 (TIRAMISU) o superiore, il permesso POST_NOTIFICATIONS è richiesto.
            // Controlla se il permesso POST_NOTIFICATIONS non è già stato concesso.
            if (ContextCompat.checkSelfPermission (requireContext(),
                    POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Se il permesso non è concesso, richiede il permesso all'utente.
                requestNotificationPermission()
            }
        }
        // Per le versioni di Android precedenti ad Android 13, il permesso per le notifiche
        // non è richiesto esplicitamente dall'app (è concesso di default).
        else {
            Toast.makeText(context, "No need permission", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }
    //Questo metodo viene chiamato quando la vista del Fragment sta per essere distrutta.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

/**
 * `requestPermissionLauncher` è un'istanza di `ActivityResultLauncher` utilizzata
 * per richiedere un permesso all'utente e gestire il risultato della richiesta.
 * Viene registrato con `ActivityResultContracts.RequestPermission()` che è un contratto
 * standard per richiedere un singolo permesso.
 * Il blocco lambda viene eseguito quando l'utente risponde alla richiesta di permesso.
 */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){ isGranted: Boolean ->
        if(isGranted){
            Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()

        }

    }
    /**
     * Questo metodo avvia la richiesta del permesso per le notifiche.
     * Controlla nuovamente la versione dell'SDK per assicurarsi che la richiesta sia pertinente.
     */
    private fun requestNotificationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(POST_NOTIFICATIONS)
        }
    }

}