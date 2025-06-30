package com.project.roomdb_replica_ufficiale.fragments.add

import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.project.roomdb_replica_ufficiale.R
import com.project.roomdb_replica_ufficiale.data.ingrediente.IngredienteQuantificato
import com.project.roomdb_replica_ufficiale.data.istruzione.Istruzione
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta
import com.project.roomdb_replica_ufficiale.data.ricetta.RicettaViewModel
import com.project.roomdb_replica_ufficiale.databinding.FragmentAddBinding
import com.project.roomdb_replica_ufficiale.relations.ricettaIngredienteRelation.RicettaIngrediente


class AddFragment : Fragment() {

    //PRIOPRIETA
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    var ingrList: MutableList<IngredienteQuantificato> = mutableListOf()
    var ingrStep: MutableList<String> = mutableListOf()

    val allergeniEuropei = listOf(
        "Gluten", "Crustaceans", "Eggs", "Fish", "Peanuts",
        "Soya", "Milk", "Tree nuts", "Celery", "Mustard",
        "Sesame", "Sulphites", "Lupin", "Molluscs"
    )

    private lateinit var mRecipeViewModel: RicettaViewModel //per gestione inserimento in db

    /*
    gestione del callback per tornare indietro. Inizialmente a false, quando iniziano delle
    modifiche il callback si attiva e quando chiamato fa partire un AlertDialog:

    response Positiva: prova a salvare i dati
    response negativa: disattiva il callback, compie l'azione di tornare indietro avvertendo di non aver salvato
    responde neutrale: rimane nella schermata senza fare nulla.
     */
    private val callback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            AlertDialog.Builder(requireContext()).apply {
                setTitle("You are leaving without saving")
                setMessage("Save so you don't lose your changes")
                setPositiveButton("Save") { _, _ ->
                    insertDataToDatabase()
                }
                setNegativeButton("Delete") { _, _ ->
                    Toast.makeText(requireContext(), "Unsaved recipe", Toast.LENGTH_SHORT).show()
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                setNeutralButton("Cancel") { _, _ ->
                }
                setCancelable(false)
            }.show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //ottengo il contesto per comodità
        var ctx = requireContext()

        //Imposto binding e inizializzo viewModel
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val view = binding.root

        mRecipeViewModel = ViewModelProvider(this).get(RicettaViewModel::class.java)

        /*
        gestione dei chipGroup
         */
        val chipGroup = binding.chipGroupAllergeni
        for (allergene in allergeniEuropei) {
            val chip = Chip(ctx).apply {
                text = allergene
                isCheckable = true
                isClickable = true
            }
            chipGroup.addView(chip)
        }

        //azione compiuta da addbutton
        binding.addBtn.setOnClickListener {
            insertDataToDatabase()
        }

        //GESTIONE ADAPTER INGREDIENT
        val animator = DefaultItemAnimator()
        animator.removeDuration = 0 //per un animazione più veloce
        val adapterIngrediente = AdapterIngrediente(ingrList)

        adapterIngrediente.setOnItemClickListener(object : AdapterIngrediente.OnItemClickListener { //elemento listener
            /*
            funzione che deve gestire la rimozione di un elemento. l'adapter deve rimuovere l'elemento
            in posizione pos e aggiornare che è stato rimosso l'elemento in posizione pos
             */
            override fun onItemClick(pos: Int) {
                adapterIngrediente.ingrList.removeAt(pos)
                adapterIngrediente.notifyItemRemoved(pos)
                adapterIngrediente.notifyItemRangeChanged(pos, adapterIngrediente.itemCount-pos)
            }
        })

        binding.ingredientContainer.apply {
            adapter  = adapterIngrediente
            layoutManager = LinearLayoutManager(ctx)
            itemAnimator = animator
        }

        /*
        gestione del bottone di inserimento in recycler view: dopo aver inserito l'elemento devo aggiornare
        l'adapter informare che in fondo alla lista è stato aggiunto un elemento
         */
        binding.addIngredientBtn.setOnClickListener {
            ingrList.add(IngredienteQuantificato("",""))
            adapterIngrediente.notifyItemInserted(adapterIngrediente.itemCount - 1)
        }

        //GESTIONE DI ADAPTER STEP
        val adapterStep = AdapterStep(ingrStep)
        adapterStep.setOnItemClickListener(object : AdapterStep.OnItemClickListener {
            //anche se poco ottimizzato devo informare l'intera lista che l'elemento è stato rimosso
            //perche devo aggiornare gli indici di ogni elemento
            override fun onItemClick(pos: Int) {
                adapterStep.stepList.removeAt(pos)
                adapterStep.notifyDataSetChanged()
            }
        })

        binding.stepContainer.adapter = adapterStep
        binding.stepContainer.layoutManager = LinearLayoutManager(ctx)

        binding.addStepBtn.setOnClickListener {
            ingrStep.add("")
            adapterStep.notifyItemInserted(adapterStep.itemCount - 1)
        }

        //GESTIONE SPINNER
        /*
        creo l'adapter usando la risorsa array level_array e impostante il layout del tipo
        simple_spinner_item.

         */
        ArrayAdapter.createFromResource(ctx, R.array.level_array,
            android.R.layout.simple_spinner_item).also { spinnerAdapter ->
                //impostazione di visualizzazione dello spinner
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.addRecipeLevelEt.adapter = spinnerAdapter
        }

        //quando viene selezionato un elemento
        binding.addRecipeLevelEt.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            var launch = true
            //se un elemento viene selezionato
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int,
                                        id: Long) {
                if(launch){
                    launch = false //per ignorare la prima selezione ossia quella in cui viene messo il valore di base
                } else {
                    callback.isEnabled = true //cambio il callback
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        //stessa gestione per quando riguarda lo spinner delle categorie
        ArrayAdapter.createFromResource(ctx, R.array.category_array,
            android.R.layout.simple_spinner_item).also { spinnerAdapter ->
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.addRecipeCategoryEt.adapter = spinnerAdapter
        }

        binding.addRecipeCategoryEt.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            var launch = true
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int,
                                        id: Long) {
                if(launch){
                    launch = false
                } else {
                    callback.isEnabled = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        //aggiorno i callback quando gli editText vengono modificati
        binding.addDurationEt.doAfterTextChanged { callback.isEnabled = true }
        binding.addRecipeNameEt.doAfterTextChanged { callback.isEnabled = true }
        binding.addRecipeDescriptionEt.doAfterTextChanged { callback.isEnabled = true }

        //aggiungo il call back al Dispatcher che gestisce l'azione back ossia : onBackPressedDispatcher
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*
    funzione che inserisce i valori all'interno del db
     */
    private fun insertDataToDatabase() {
        //ottengo i valori contenuti in tutti i campi Editext
        val nomeRicetta = binding.addRecipeNameEt.text.toString()
        val descrizione = binding.addRecipeDescriptionEt.text.toString()
        val durata = binding.addDurationEt.text
        //dagli spinner
        val posLevel = binding.addRecipeLevelEt.lastVisiblePosition
        val posCategoria = binding.addRecipeCategoryEt.lastVisiblePosition
        //se la posizione dell'elemento dello spinner è zero vuol dire che non è stato selezionato nulla dunque
        //informo l'utente che non completo l'operazione e finisco la funzione (return)
        if(posLevel == 0 || posCategoria == 0) {
            Toast.makeText(requireContext(), "Please fill put all fields.", Toast.LENGTH_LONG).show()
            return
        }
        //altrimenti salvo i valori dello spinner
        val livello = binding.addRecipeLevelEt.selectedItem.toString()
        val categoria = binding.addRecipeCategoryEt.selectedItem.toString()

        //mi salvo ultima modifica e esecuzione
        val ultimaModifica = System.currentTimeMillis()
        val ultimaEsecuzione = System.currentTimeMillis()

        //conta quante volte viene eseguita
        val count = 0

        //dalla recyclerView ottengo tutti i valori degli Step
        val istruzioni = mutableListOf<Istruzione>()
        for (i in 0 until binding.stepContainer.childCount) {
            val textStep = ingrStep[i].toString().trim()
            if(textStep.isNotEmpty()){
                val istr = Istruzione(0, 0, i+1, textStep)
                istruzioni.add(istr)
            }
        }

        //dalla recyclerView ottengo tutti i valori degli Ingredienti
        val ingredientiList = mutableListOf<RicettaIngrediente>()
        for(ingrediente in ingrList) {
            if(ingrediente.nomeIngrediente.isNotEmpty() && ingrediente.quantita.isNotEmpty()){
                val ric = RicettaIngrediente(0, ingrediente.nomeIngrediente, ingrediente.quantita)
                Log.d("RicettaIngrediente", ingrediente.nomeIngrediente)
                ingredientiList.add(ric)
            }
        }

        //mi salvo gli elementi selezionati nei Chip
        val allergeniSelezionati = mutableListOf<String>()
        for (i in 0 until binding.chipGroupAllergeni.childCount) {
            val chip = binding.chipGroupAllergeni.getChildAt(i) as Chip
            if (chip.isChecked) {
                allergeniSelezionati.add(chip.text.toString())
            }
        }

        //se i valori di input sono corretti
        if(inputCheck(nomeRicetta, durata, livello, categoria, descrizione)){
            //Creo oggetto ricetta
            val ricetta = Ricetta(0L, nomeRicetta, Integer.parseInt(durata.toString()), livello, categoria, descrizione, ultimaModifica, ultimaEsecuzione, count, allergeniSelezionati)
            //Aggiungo al db
            mRecipeViewModel.inserisciRicettaCompleta(ricetta, istruzioni, ingredientiList)
            //mRecipeViewModel.nuovaRicetta(ricetta)
            Toast.makeText(requireContext(), "Succesfully added!", Toast.LENGTH_LONG).show()
            //Navigate Back
            findNavController().popBackStack()
            //Svuota lo stack finche trova Home in modo tale che da List ritorni a Home
            val navOptions = navOptions {
                popUpTo(R.id.homeFragment) { inclusive = false }
            }
            findNavController().navigate(
                R.id.listFragment,
                null,
                navOptions
            )
        }else{
            Toast.makeText(requireContext(), "Please fill put all fields.", Toast.LENGTH_LONG).show()
        }
    }


    /*
    funzione che fa un check per verificare se i campi sono completi oppure se qualcuno è rimasto vuoto
     */
    private fun inputCheck(nomeRicetta: String, durata: Editable, livello: String, categoria: String, descrizione: String): Boolean{

        if(nomeRicetta.isEmpty() || durata.isEmpty() || livello.isEmpty() || categoria.isEmpty() || descrizione.isEmpty()){
            return false
        } else {
            return true
        }

    }

}