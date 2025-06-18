package com.project.roomdb_replica_ufficiale.fragments.add

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mRecipeViewModel: RicettaViewModel

    var ingrList: MutableList<IngredienteQuantificato> = mutableListOf()
    var ingrStep: MutableList<String> = mutableListOf()

    val allergeniEuropei = listOf(
        "Glutine", "Crostacei", "Uova", "Pesce", "Arachidi",
        "Soia", "Latte", "Frutta a guscio", "Sedano", "Senape",
        "Sesamo", "Anidride solforosa", "Lupini", "Molluschi"
    )

    /* Back-press handling */
    private val callback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Stai uscendo senza salvare")
                setMessage("Salva per non perdere le modifiche")
                setPositiveButton("Save") { _, _ ->
                    insertDataToDatabase()
                    Toast.makeText(requireContext(), "clicked save", Toast.LENGTH_SHORT).show()
                }
                setNegativeButton("Delete") { _, _ ->
                    Toast.makeText(requireContext(), "Ricetta non salvata", Toast.LENGTH_SHORT).show()
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

        var ctx = requireContext()

        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val view = binding.root

        val chipGroup = binding.chipGroupAllergeni
        for (allergene in allergeniEuropei) {
            val chip = Chip(requireContext()).apply {
                text = allergene
                isCheckable = true
                isClickable = true
            }
            chipGroup.addView(chip)
        }

        mRecipeViewModel = ViewModelProvider(this).get(RicettaViewModel::class.java)

        binding.addBtn.setOnClickListener {
            insertDataToDatabase()
        }


        //GESTIONE ADAPTER
        val animator = DefaultItemAnimator()
        animator.removeDuration = 0
        val adapterIngrediente = AdapterIngrediente(ingrList)

        adapterIngrediente.setOnItemClickListener(object : AdapterIngrediente.OnItemClickListener {
            override fun onItemClick(pos: Int) {
                Log.d("ON ITEM CLICK", "should delete elem $pos")
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

        binding.addIngredientBtn.setOnClickListener {
            Log.d("PRE ADD INGR", "${adapterIngrediente.itemCount}")
            ingrList.add(IngredienteQuantificato("",""))
            adapterIngrediente.notifyItemInserted(adapterIngrediente.itemCount - 1)
            Log.d("SIZE INGR", "${adapterIngrediente.itemCount}")
        }

        val adapterStep = AdapterStep(ingrStep)
        adapterStep.setOnItemClickListener(object : AdapterStep.OnItemClickListener {
            override fun onItemClick(pos: Int) {
                Log.d("ON ITEM CLICK", "should delete")
                adapterStep.stepList.removeAt(pos)
                adapterStep.notifyDataSetChanged()
                Log.d("POST REMOVED", "${adapterIngrediente.itemCount}")
            }
        })

        binding.stepContainer.adapter = adapterStep
        binding.stepContainer.layoutManager = LinearLayoutManager(ctx)

        binding.addStepBtn.setOnClickListener {
            ingrStep.add("")
            adapterStep.notifyItemInserted(adapterStep.itemCount - 1)
        }

        //GESTIONE SPINNER
        ArrayAdapter.createFromResource(ctx, R.array.level_array,
            android.R.layout.simple_spinner_item).also { spinnerAdapter ->
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.addRecipeLevelEt.adapter = spinnerAdapter
        }

        binding.addRecipeLevelEt.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            var launch = true
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int,
                                        id: Long) {
                if(launch){
                    launch = false
                } else {
                    callback.isEnabled = true
                    Log.d("SPINNER", "selezionato")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("SPINNER", "non selezionato")
            }

        }

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
                    Log.d("SPINNER", "selezionato")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("SPINNER", "non selezionato")
            }

        }

        binding.addDurationEt.doAfterTextChanged { callback.isEnabled = true }
        binding.addRecipeNameEt.doAfterTextChanged { callback.isEnabled = true }
        binding.addRecipeDescriptionEt.doAfterTextChanged { callback.isEnabled = true }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun insertDataToDatabase() {
        val nomeRicetta = binding.addRecipeNameEt.text.toString()
        val descrizione = binding.addRecipeDescriptionEt.text.toString()
        val durata = binding.addDurationEt.text

        val posLevel = binding.addRecipeLevelEt.lastVisiblePosition
        val posCategoria = binding.addRecipeCategoryEt.lastVisiblePosition

        if(posLevel == 0 || posCategoria == 0) {
            Toast.makeText(requireContext(), "Please fill put all fields.", Toast.LENGTH_LONG).show()
            return
        }
        val livello = binding.addRecipeLevelEt.selectedItem.toString()
        val categoria = binding.addRecipeCategoryEt.selectedItem.toString()

        val ultimaModifica = System.currentTimeMillis()
        val ultimaEsecuzione = System.currentTimeMillis()

        val count = 0

        val istruzioni = mutableListOf<Istruzione>()
        for (i in 0 until binding.stepContainer.childCount) {
            val textStep = ingrStep[i].toString().trim()
            if(textStep.isNotEmpty()){
                val istr = Istruzione(0, nomeRicetta, i+1, textStep)
                istruzioni.add(istr)
            }
        }


        val ingredientiList = mutableListOf<RicettaIngrediente>()
        for(ingrediente in ingrList) {
            if(ingrediente.nomeIngrediente.isNotEmpty() && ingrediente.quantita.isNotEmpty()){
                val ric = RicettaIngrediente(nomeRicetta, ingrediente.nomeIngrediente, ingrediente.quantita)
                Log.d("RicettaIngrediente", ingrediente.nomeIngrediente)
                ingredientiList.add(ric)
            }
        }

//        for (i in 0 until binding.ingredientContainer.childCount) {
//            val row = binding.ingredientContainer.getChildAt(i)
//
//            if (row is LinearLayout && row.childCount == 2) {
//                val nomeEt = row.getChildAt(0) as? EditText
//                val quantitaEt = row.getChildAt(1) as? EditText
//
//                val nome = nomeEt?.text.toString().trim()
//                val quantita = quantitaEt?.text.toString().trim()
//
//                if (nome.isNotEmpty() && quantita.isNotEmpty()) {
//                    ingredientiList.add(RicettaIngrediente(nomeRicetta, nome, quantita))
//                }
//            }
//        }

        val allergeniSelezionati = mutableListOf<String>()
        for (i in 0 until binding.chipGroupAllergeni.childCount) {
            val chip = binding.chipGroupAllergeni.getChildAt(i) as Chip
            if (chip.isChecked) {
                allergeniSelezionati.add(chip.text.toString())
            }
        }


        if(inputCheck(nomeRicetta, durata, livello, categoria, descrizione)){
            //Create Recipe Object
            val ricetta = Ricetta(nomeRicetta, Integer.parseInt(durata.toString()), livello, categoria, descrizione, ultimaModifica, ultimaEsecuzione, count, allergeniSelezionati)
            //Add Data to Database
            mRecipeViewModel.inserisciRicettaCompleta(ricetta, istruzioni, ingredientiList)
            //mRecipeViewModel.nuovaRicetta(ricetta)
            Toast.makeText(requireContext(), "Succesfully added!", Toast.LENGTH_LONG).show()
            //Navigate Back
            findNavController().navigate(R.id.action_addFragment_to_listFragment)

        }else{
            Toast.makeText(requireContext(), "Please fill put all fields.", Toast.LENGTH_LONG).show()
        }
    }



    private fun inputCheck(nomeRicetta: String, durata: Editable, livello: String, categoria: String, descrizione: String): Boolean{

        if(nomeRicetta.isEmpty() || durata.isEmpty() || livello.isEmpty() || categoria.isEmpty() || descrizione.isEmpty()){
            return false
        } else {
            return true
        }

//        return !(TextUtils.isEmpty(nomeRicetta) && durata.isEmpty() && TextUtils.isEmpty(livello) && TextUtils.isEmpty(categoria) && TextUtils.isEmpty(descrizione))
    }

    override fun onSaveInstanceState(outState: Bundle) {

        super.onSaveInstanceState(outState)
    }
}