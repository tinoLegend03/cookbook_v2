package com.project.roomdb_replica_ufficiale.fragments.update

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.project.roomdb_replica_ufficiale.R
import com.project.roomdb_replica_ufficiale.data.istruzione.Istruzione
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta
import com.project.roomdb_replica_ufficiale.data.ricetta.RicettaViewModel
import com.project.roomdb_replica_ufficiale.databinding.FragmentUpdateBinding
import com.project.roomdb_replica_ufficiale.relations.ricettaIngredienteRelation.RicettaIngrediente
import kotlinx.coroutines.launch

/**
 * Fragment di modifica ricetta.
 * Tutti i campi possono essere cambiati, compresi
 * step dinamici, ingredienti e allergeni.
 */
class UpdateFragment : Fragment() {

    /* ----------------- Setup base & binding ------------------- */

    private val args by navArgs<UpdateFragmentArgs>()    // ricetta da modificare
    private lateinit var mRecipeViewModel: RicettaViewModel

    /* ---------- ViewBinding --------------------------------------- */
    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    /* ----------------- onCreateView --------------------------- */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /* -------- Inflate & ViewModel -------------------------- */
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        val view = binding.root
        mRecipeViewModel = ViewModelProvider(this).get(RicettaViewModel::class.java)

        /* Pre-popola campi con i dati della ricetta */
        binding.updateRecipeNameEt.setText(args.currentRecipe.nomeRicetta)
        binding.updateDurationEt.setText(args.currentRecipe.durata.toString())
        binding.updateRecipeDescriptionEt.setText(args.currentRecipe.descrizione)


        /* Lista allergeni a livello europeo */
        val allergeniEuropei = listOf(
            "Glutine", "Crostacei", "Uova", "Pesce", "Arachidi",
            "Soia", "Latte", "Frutta a guscio", "Sedano", "Senape",
            "Sesamo", "Anidride solforosa", "Lupini", "Molluschi"
        )


        /* Si creano e impostano gli Spinner */
        ArrayAdapter.createFromResource(requireContext(), R.array.level_array,
                android.R.layout.simple_spinner_item).also { spinnerAdapter ->
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.updateRecipeLevelEt.adapter = spinnerAdapter
        }
        ArrayAdapter.createFromResource(requireContext(), R.array.category_array,
            android.R.layout.simple_spinner_item).also { spinnerAdapter ->
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.updateRecipeCategoryEt.adapter = spinnerAdapter
        }

        for(i in 0 until binding.updateRecipeLevelEt.count){
            val toSelect = binding.updateRecipeLevelEt.getItemAtPosition(i)
            if (toSelect == args.currentRecipe.livello){
                binding.updateRecipeLevelEt.setSelection(i)
            }
        }
        for(i in 0 until binding.updateRecipeCategoryEt.count){
            val toSelect = binding.updateRecipeCategoryEt.getItemAtPosition(i)

            if (toSelect == args.currentRecipe.categoria){
                binding.updateRecipeCategoryEt.setSelection(i)
            }
        }

        val nomeRicetta = args.currentRecipe.nomeRicetta


        /* -------- Carica istruzioni dinamiche ---------------- */

        val idRicetta = args.currentRecipe.idRicetta
        var numeroStep = 0              // contatore per nuovo step

        mRecipeViewModel.getRicettaConIstruzioni(idRicetta).observe(viewLifecycleOwner) { dati ->

            val steps = dati.istruzioni.sortedBy { it.numero }
            binding.updateStepContainer.removeAllViews()
            steps.forEach {

                val layout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 8, 0, 8) }
                }

                /* EditText dove viene visualizzato lo step */
                val stepView = EditText(requireContext()).apply {
                    setText("${it.descrizione}")
                    hint = "Step ${it.numero}"
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                /* Bottone per eliminare uno step */
                val deleteBtn = ImageButton(requireContext()).apply {
                    setImageResource(android.R.drawable.ic_menu_delete)
                    setBackgroundResource(0)
                    imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_color_tint)
                    setOnClickListener {
                        binding.updateStepContainer.removeView(layout)
                    }
                }

                layout.addView(stepView)
                layout.addView(deleteBtn)
                binding.updateStepContainer.addView(layout)


            }

            numeroStep = dati.istruzioni.size+1
        }

        // Aggiunge un campo “Step X” vuoto in fondo
        binding.addStepUpdateBtn.setOnClickListener{
            aggiungiCampoStep(numeroStep)
            numeroStep++
        }
        //////////// END SEZIONE INSTRUZIONI ////////////////////




        /* -------- Carica ingredienti dinamici ---------------- */

        mRecipeViewModel.getIngredientiConQuantitaPerRicetta(idRicetta).observe(viewLifecycleOwner) { ingredienti ->
            binding.updateIngredientContainer.removeAllViews()


            /** Ricostruisce i campi ingredienti: nome + quantità + delete */
            ingredienti.forEach {
                val layout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 8, 0, 8)
                    }
                }
                val ingredienteView = EditText(requireContext()).apply {
                    setText("${it.nomeIngrediente}")
                    hint = "Ingrediente"
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                val quantitaView = EditText(requireContext()).apply {
                    setText("${it.quantita}")
                    hint = "Quantità"
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                val deleteBtn = ImageButton(requireContext()).apply {
                    setImageResource(android.R.drawable.ic_menu_delete)
                    setBackgroundResource(0)
                    imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_color_tint)
                    setOnClickListener {
                        binding.updateIngredientContainer.removeView(layout)
                    }
                }

                layout.addView(ingredienteView)
                layout.addView(quantitaView)
                layout.addView(deleteBtn)
                binding.updateIngredientContainer.addView(layout)
            }
        }


        binding.addIngredientUpdateBtn.setOnClickListener{
            aggiungiCampoIngrediente()
        }

        //////////// END SEZIONE INGREDIENTI ////////////////////


        /* -------- Allergeni (chip) ---------------------------- */
        val allergeniRicetta = args.currentRecipe.allergeni ?: emptyList()

        /** Popola la ChipGroup degli allergeni con selezione multipla. */
        allergeniEuropei.forEach { allergene ->
            val chip = Chip(requireContext()).apply {
                text = allergene
                isCheckable = true
                isChecked = allergeniRicetta.contains(allergene)
            }
            binding.chipGroupAllergeniUpdate.addView(chip)
        }
        //////////// END SEZIONE ALLERGENI ////////////////////



        /* -------- Pulsanti update / delete ------------------- */

        binding.updateBtn.setOnClickListener{
            updateItem()
        }

        binding.deleteBtn.setOnClickListener{
            deleteRecipe()
        }



        /* ----------- Controlla quando mostrare l'AlertDialog in caso di uscita senza salvataggio -----*/

        binding.updateRecipeNameEt.doAfterTextChanged { backCallback.isEnabled = true }
        binding.updateRecipeDescriptionEt.doAfterTextChanged { backCallback.isEnabled = true }
        binding.updateDurationEt.doAfterTextChanged { backCallback.isEnabled = true }

        // Spinner Livello
        binding.updateRecipeLevelEt.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                private var first = true
                override fun onItemSelected(
                    parent: AdapterView<*>, v: View?, pos: Int, id: Long
                ) {
                    if (!first) backCallback.isEnabled = true
                    first = false
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }

        //Spinner categoria
        binding.updateRecipeCategoryEt.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                private var first = true
                override fun onItemSelected(
                    parent: AdapterView<*>, v: View?, pos: Int, id: Long
                ) {
                    if (!first) backCallback.isEnabled = true
                    first = false
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }


        // Check su aggiunta ingredienti per l'AlertDialog
        binding.addIngredientUpdateBtn.setOnClickListener {
            aggiungiCampoIngrediente()
            backCallback.isEnabled = true
        }

        // Check su aggiunta step per l'AlertDialog
        binding.addStepUpdateBtn.setOnClickListener {
            aggiungiCampoStep(numeroStep)
            numeroStep++
            backCallback.isEnabled = true
        }


        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, backCallback)

        return view


    }

    /* ---------------- Helpers UI  ----------------------- */


    /* ---------- updateItem(): valida input e salva ----------------------- */
    private fun updateItem(){
        val nomeRicetta = binding.updateRecipeNameEt.text.toString()
        val durata = Integer.parseInt(binding.updateDurationEt.text.toString())
        val descrizione = binding.updateRecipeDescriptionEt.text.toString()
        val ultimaModifica = System.currentTimeMillis()
        val ultimaEsecuzione = System.currentTimeMillis()
        val count = args.currentRecipe.count

        val posLivello = binding.updateRecipeLevelEt.lastVisiblePosition
        val posCategoria = binding.updateRecipeCategoryEt.lastVisiblePosition

        if(posLivello == 0 || posCategoria == 0) {
            Toast.makeText(requireContext(), "Please fill put all fields.", Toast.LENGTH_LONG).show()
            return
        }

        val livello = binding.updateRecipeLevelEt.selectedItem.toString()
        val categoria = binding.updateRecipeCategoryEt.selectedItem.toString()

        /* Si aggiungono tutte le istruzioni (nuove comprese) ad una lista */
        val istruzioni = mutableListOf<Istruzione>()
        istruzioni.clear()
        var stepNumber = 1
        for (i in 0 until binding.updateStepContainer.childCount) {
            val row = binding.updateStepContainer.getChildAt(i)
            if (row is LinearLayout && row.childCount > 0) {
                val stepEt = row.getChildAt(0) as? EditText
                val testo = stepEt?.text.toString().trim()
                if (testo.isNotEmpty()) {
                    istruzioni.add(Istruzione(0, args.currentRecipe.idRicetta, stepNumber++, testo))
                }
            }
        }



        /* Si aggiungono tutte gli ingredienti (nuovi comprese) ad un'unica lista */
        val ingredientiList = mutableListOf<RicettaIngrediente>()

        for (i in 0 until binding.updateIngredientContainer.childCount) {
            val row = binding.updateIngredientContainer.getChildAt(i)
            if (row is LinearLayout && row.childCount >= 2) {
                val nomeEt = row.getChildAt(0) as? EditText
                val quantitaEt = row.getChildAt(1) as? EditText

                val nome = nomeEt?.text.toString().trim()
                val quantita = quantitaEt?.text.toString().trim()

                if (nome.isNotEmpty() && quantita.isNotEmpty()) {
                    ingredientiList.add(RicettaIngrediente(args.currentRecipe.idRicetta, nome, quantita))
                }
            }
        }

        /* Si aggiungono tutte gli allergeni (nuovi compresi) ad un'unica lista */
        val allergeniSelezionati = mutableListOf<String>()
        for (i in 0 until binding.chipGroupAllergeniUpdate.childCount) {
            val chip = binding.chipGroupAllergeniUpdate.getChildAt(i) as Chip
            if (chip.isChecked) {
                allergeniSelezionati.add(chip.text.toString())
            }
        }

        // Controllo campi obbligatori
        if(inputCheck(nomeRicetta, binding.updateDurationEt.text, livello, categoria, descrizione)) {

            lifecycleScope.launch {
                val nomeGiaUsato = mRecipeViewModel.esisteNomeDuplicato(
                    nomeRicetta,
                    args.currentRecipe.idRicetta
                )

                if (nomeGiaUsato) {
                    Toast.makeText(
                        requireContext(),
                        "Esiste già una ricetta con questo nome.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val updatedRecipe = Ricetta(
                        args.currentRecipe.idRicetta,
                        nomeRicetta,
                        durata,
                        livello,
                        categoria,
                        descrizione,
                        ultimaModifica,
                        ultimaEsecuzione,
                        count,
                        allergeniSelezionati
                    )
                    mRecipeViewModel.aggiornaRicettaCompleta(updatedRecipe, istruzioni, ingredientiList)
                    Toast.makeText(requireContext(), "Updated Successfully!", Toast.LENGTH_SHORT).show()
                    backCallback.isEnabled = false          // evita che il dialog compaia di nuovo
                    findNavController().popBackStack()
                }
            }

        } else {
            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show()
        }
    }




    /* Verifica campi principali */
    private fun inputCheck(nomeRicetta: String, durata: Editable, livello: String, categoria: String, descrizione: String): Boolean{
        return !(TextUtils.isEmpty(nomeRicetta) && durata.isEmpty() && TextUtils.isEmpty(livello) && TextUtils.isEmpty(categoria) && TextUtils.isEmpty(descrizione))
    }






    /* ---------- Aggiunta dinamica campo step ------------------------------- */

    private fun aggiungiCampoStep(numero: Int) {

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 8) }
        }

        /* Si crea lo spazio per il nuovo step */
        val nuovoStep = EditText(requireContext()).apply {
            hint = "Step"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        }

        /* Si aggiunge il botton eper eliminare lo step */
        val deleteBtn = ImageButton(requireContext()).apply {
            setImageResource(android.R.drawable.ic_menu_delete)
            setBackgroundResource(0)
            imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_color_tint)
            setOnClickListener {
                binding.updateStepContainer.removeView(layout)
            }
        }

        /* Elementi aggiunti alla View */
        layout.addView(nuovoStep)
        layout.addView(deleteBtn)
        binding.updateStepContainer.addView(layout)


    }


    /* ---------- Aggiunta dinamica campo ingredienti ------------------------------- */

    private fun aggiungiCampoIngrediente() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }

        //Nome del nuovo ingrediente
        val nomeIngredienteEt = EditText(requireContext()).apply {
            hint = "Ingrediente"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Quantità per il nuovo ingrediente
        val quantitaEt = EditText(requireContext()).apply {
            hint = "Quantità"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Bottone per eliminare l'ingrediente
        val deleteBtn = ImageButton(requireContext()).apply {
            setImageResource(android.R.drawable.ic_menu_delete)
            setBackgroundResource(0)
            imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_color_tint)
            setOnClickListener {
                binding.updateIngredientContainer.removeView(layout)
            }
        }

        /* Elementi aggiunti alla View */
        layout.addView(nomeIngredienteEt)
        layout.addView(quantitaEt)
        layout.addView(deleteBtn)

        binding.updateIngredientContainer.addView(layout)
    }


    /* ---------- Delete ---------------------------------------------------- */
    private fun deleteRecipe() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes"){_, _->
            mRecipeViewModel.eliminaRicetta(args.currentRecipe)
            Toast.makeText(requireContext(), "Successfully removed: ${args.currentRecipe.nomeRicetta}", Toast.LENGTH_SHORT).show()
            backCallback.isEnabled = false          // evita che il dialog compaia di nuovo
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        builder.setNegativeButton("No"){_, _-> }
        builder.setTitle("Delete ${args.currentRecipe.nomeRicetta}?")
        builder.setMessage("Are you sure you want to delete ${args.currentRecipe.nomeRicetta}")
        builder.create().show()
    }


    /**
     * Mostra un AlertDialog se l’utente torna indietro con modifiche non salvate.
     * Viene abilitato (= isEnabled = true) solo quando l’utente cambia qualcosa.
     */
    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {

            AlertDialog.Builder(requireContext())
                .setTitle("Modifiche non salvate")
                .setMessage("Vuoi salvare prima di uscire?")
                .setPositiveButton("Salva") { _, _ ->
                    // Prova a salvare; se i dati sono validi chiude la schermata
                    updateItem()          // già contiene tutte le validazioni
                }
                .setNegativeButton("Esci") { _, _ ->
                    // Abbandona le modifiche e torna alla schermata precedente
                    isEnabled = false     // evita loop
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                .setNeutralButton("Annulla", null)
                .show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}


