package com.project.roomdb_replica_ufficiale.fragments.update

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.project.roomdb_replica_ufficiale.R
import com.project.roomdb_replica_ufficiale.data.istruzione.Istruzione
import com.project.roomdb_replica_ufficiale.data.ricetta.Ricetta
import com.project.roomdb_replica_ufficiale.data.ricetta.RicettaViewModel
import com.project.roomdb_replica_ufficiale.databinding.FragmentUpdateBinding
import com.project.roomdb_replica_ufficiale.relations.ricettaIngredienteRelation.RicettaIngrediente


class UpdateFragment : Fragment() {

    private val args by navArgs<UpdateFragmentArgs>()
    private lateinit var mRecipeViewModel: RicettaViewModel

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        val view = binding.root

        val allergeniEuropei = listOf(
            "Glutine", "Crostacei", "Uova", "Pesce", "Arachidi",
            "Soia", "Latte", "Frutta a guscio", "Sedano", "Senape",
            "Sesamo", "Anidride solforosa", "Lupini", "Molluschi"
        )

        mRecipeViewModel = ViewModelProvider(this).get(RicettaViewModel::class.java)

        binding.updateRecipeNameEt.setText(args.currentRecipe.nomeRicetta)
        binding.updateDurationEt.setText(args.currentRecipe.durata.toString())
        binding.updateRecipeDescriptionEt.setText(args.currentRecipe.descrizione)

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

        //////////// START SEZIONE INSTRUZIONI ////////////////////

        var numeroStep = 0

        mRecipeViewModel.getRicettaConIstruzioni(nomeRicetta).observe(viewLifecycleOwner) { dati ->

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

                val stepView = EditText(requireContext()).apply {
                    setText("${it.descrizione}")
                    hint = "Step ${it.numero}"
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val deleteBtn = ImageButton(requireContext()).apply {
                    setImageResource(android.R.drawable.ic_menu_delete)
                    setBackgroundResource(0)
                    setOnClickListener {
                        binding.updateStepContainer.removeView(layout)
                    }
                }

                layout.addView(stepView)
                layout.addView(deleteBtn)
                binding.updateStepContainer.addView(layout)

                //binding.updateStepContainer.addView(stepView)
            }

            numeroStep = dati.istruzioni.size+1
        }


        binding.addStepUpdateBtn.setOnClickListener{
            aggiungiCampoStep(numeroStep)
            numeroStep++
        }
        //////////// END SEZIONE INSTRUZIONI ////////////////////




        //////////// START SEZIONE INGREDIENTI ////////////////////

        mRecipeViewModel.getIngredientiConQuantitaPerRicetta(nomeRicetta).observe(viewLifecycleOwner) { ingredienti ->
            binding.updateIngredientContainer.removeAllViews()



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


        /////////// START SEZIONE ALLERGENI ////////////////////
        val allergeniRicetta = args.currentRecipe.allergeni ?: emptyList()

        allergeniEuropei.forEach { allergene ->
            val chip = Chip(requireContext()).apply {
                text = allergene
                isCheckable = true
                isChecked = allergeniRicetta.contains(allergene)
            }
            binding.chipGroupAllergeniUpdate.addView(chip)
        }
        //////////// END SEZIONE ALLERGENI ////////////////////





        binding.updateBtn.setOnClickListener{
            updateItem()
        }

        binding.deleteBtn.setOnClickListener{
            deleteRecipe()
        }

        return view


    }

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

        val istruzioni = mutableListOf<Istruzione>()
        istruzioni.clear()
        var stepNumber = 1
        for (i in 0 until binding.updateStepContainer.childCount) {
            val row = binding.updateStepContainer.getChildAt(i)
            if (row is LinearLayout && row.childCount > 0) {
                val stepEt = row.getChildAt(0) as? EditText
                val testo = stepEt?.text.toString().trim()
                if (testo.isNotEmpty()) {
                    istruzioni.add(Istruzione(0, nomeRicetta, stepNumber++, testo))
                }
            }
        }
        /*for (i in 0 until binding.updateStepContainer.childCount) {
            val view = binding.updateStepContainer.getChildAt(i)
            if (view is EditText) {
                val testo = view.text.toString().trim()
                if (testo.isNotEmpty()) {
                    istruzioni.add(Istruzione(0, nomeRicetta, i + 1, testo))
                }
            }
        }*/


        val ingredientiList = mutableListOf<RicettaIngrediente>()

        for (i in 0 until binding.updateIngredientContainer.childCount) {
            val row = binding.updateIngredientContainer.getChildAt(i)
            if (row is LinearLayout && row.childCount >= 2) {
                val nomeEt = row.getChildAt(0) as? EditText
                val quantitaEt = row.getChildAt(1) as? EditText

                val nome = nomeEt?.text.toString().trim()
                val quantita = quantitaEt?.text.toString().trim()

                if (nome.isNotEmpty() && quantita.isNotEmpty()) {
                    ingredientiList.add(RicettaIngrediente(nomeRicetta, nome, quantita))
                }
            }
            Log.d("TAG", "OCCHIOOOOOOOOO:: ${binding.updateIngredientContainer.childCount}")
        }

        val allergeniSelezionati = mutableListOf<String>()
        for (i in 0 until binding.chipGroupAllergeniUpdate.childCount) {
            val chip = binding.chipGroupAllergeniUpdate.getChildAt(i) as Chip
            if (chip.isChecked) {
                allergeniSelezionati.add(chip.text.toString())
            }
        }

        if(inputCheck(nomeRicetta, binding.updateDurationEt.text, livello, categoria, descrizione)){
            //Create recipe Object
            val testList = listOf("test1", "test2")
            //val updatedRecipe = Ricetta(args.currentRecipe.nomeRicetta, durata, livello, categoria, descrizione, System.currentTimeMillis(), args.currentRecipe.ultimaEsecuzione, args.currentRecipe.count, testList)
            val updatedRecipe = Ricetta(nomeRicetta, Integer.parseInt(durata.toString()), livello, categoria, descrizione, ultimaModifica, ultimaEsecuzione, count, allergeniSelezionati)
            //Update Current Recipe
            mRecipeViewModel.aggiornaRicettaCompleta(updatedRecipe, istruzioni, ingredientiList)
            Toast.makeText(requireContext(), "Updated Successfully!", Toast.LENGTH_SHORT).show()
            //Navigate Back
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }else{
            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show()
        }
    }


//    private fun insertDataToDatabase() {
//        val nomeRicetta = binding.updateRecipeNameEt.text.toString()
//        val durata = binding.updateDurationEt.text
////        val livello = binding.updateRecipeLevelEt.text.toString()
////        val categoria = binding.updateRecipeCategoryEt.text.toString()
//        val descrizione = binding.updateRecipeDescriptionEt.text.toString()
//        val ultimaModifica = System.currentTimeMillis()
//        val ultimaEsecuzione = System.currentTimeMillis()
//        val count = args.currentRecipe.count
//
//        val istruzioni = mutableListOf<Istruzione>()
//        for (i in 0 until binding.updateStepContainer.childCount) {
//            val view = binding.updateStepContainer.getChildAt(i)
//            if (view is EditText) {
//                val testo = view.text.toString().trim()
//                if (testo.isNotEmpty()) {
//                    istruzioni.add(Istruzione(0, nomeRicetta, i + 1, testo))
//                }
//            }
//        }
//
//
//        val ingredientiList = mutableListOf<RicettaIngrediente>()
//
//        for (i in 0 until binding.updateIngredientContainer.childCount) {
//            val row = binding.updateIngredientContainer.getChildAt(i)
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
//
//        val allergeniSelezionati = mutableListOf<String>()
//        /*for (i in 0 until binding.chipGroupAllergeniUpdate.childCount) {
//            val chip = binding.chipGroupAllergeniUpdate.getChildAt(i) as Chip
//            if (chip.isChecked) {
//                allergeniSelezionati.add(chip.text.toString())
//            }
//        }*/
//
//
//        if(inputCheck(nomeRicetta, durata, livello, categoria, descrizione)){
//            //Create Recipe Object
//            val ricetta = Ricetta(nomeRicetta, Integer.parseInt(durata.toString()), livello, categoria, descrizione, ultimaModifica, ultimaEsecuzione, count, allergeniSelezionati)
//            //Add Data to Database
//            mRecipeViewModel.inserisciRicettaCompleta(ricetta, istruzioni, ingredientiList)
//            //mRecipeViewModel.nuovaRicetta(ricetta)
//            Toast.makeText(requireContext(), "Succesfully added!", Toast.LENGTH_LONG).show()
//            //Navigate Back
//            findNavController().navigate(R.id.action_addFragment_to_listFragment)
//
//        }else{
//            Toast.makeText(requireContext(), "Please fill put all fields.", Toast.LENGTH_LONG).show()
//        }
//    }


    private fun inputCheck(nomeRicetta: String, durata: Editable, livello: String, categoria: String, descrizione: String): Boolean{
        return !(TextUtils.isEmpty(nomeRicetta) && durata.isEmpty() && TextUtils.isEmpty(livello) && TextUtils.isEmpty(categoria) && TextUtils.isEmpty(descrizione))
    }


    private fun deleteRecipe() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes"){_, _->
            mRecipeViewModel.eliminaRicetta(args.currentRecipe)
            Toast.makeText(requireContext(), "Successfully removed: ${args.currentRecipe.nomeRicetta}", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        builder.setNegativeButton("No"){_, _-> }
        builder.setTitle("Delete ${args.currentRecipe.nomeRicetta}?")
        builder.setMessage("Are you sure you want to delete ${args.currentRecipe.nomeRicetta}")
        builder.create().show()
    }


    private fun aggiungiCampoStep(numero: Int) {

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 8) }
        }

        val nuovoStep = EditText(requireContext()).apply {
            hint = "Step $numero"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            /*layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }*/
        }

        val deleteBtn = ImageButton(requireContext()).apply {
            setImageResource(android.R.drawable.ic_menu_delete)
            setBackgroundResource(0)
            setOnClickListener {
                binding.updateStepContainer.removeView(layout)
            }
        }

        layout.addView(nuovoStep)
        layout.addView(deleteBtn)
        binding.updateStepContainer.addView(layout)

        //binding.updateStepContainer.addView(nuovoStep)
    }


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

        val nomeIngredienteEt = EditText(requireContext()).apply {
            hint = "Ingrediente"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val quantitaEt = EditText(requireContext()).apply {
            hint = "Quantità"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val deleteBtn = ImageButton(requireContext()).apply {
            setImageResource(android.R.drawable.ic_menu_delete)
            setBackgroundResource(0)
            setOnClickListener {
                binding.updateIngredientContainer.removeView(layout)
            }
        }

        layout.addView(nomeIngredienteEt)
        layout.addView(quantitaEt)
        layout.addView(deleteBtn)

        binding.updateIngredientContainer.addView(layout)
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}


/*class UpdateFragment : Fragment() {

    private val args by navArgs<UpdateFragmentArgs>()
    private lateinit var mRecipeViewModel: RicettaViewModel

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        val view = binding.root

        mRecipeViewModel = ViewModelProvider(this).get(RicettaViewModel::class.java)

        binding.updateRecipeNameEt.setText(args.currentRecipe.nomeRicetta)
        binding.updateDurationEt.setText(args.currentRecipe.durata.toString())
        binding.updateRecipeDescriptionEt.setText(args.currentRecipe.descrizione)
        //binding.updateRecipeNameEt.setText(args.currentRecipe.nomeRicetta)
        //binding.updateRecipeNameEt.setText(args.currentRecipe.nomeRicetta)
        //binding.updateRecipeNameEt.setText(args.currentRecipe.nomeRicetta)

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

        binding.updateBtn.setOnClickListener{
            updateItem()
        }

        binding.deleteBtn.setOnClickListener{
            deleteRecipe()
        }

        /*binding.deleteBtn.setOnClickListener{
            deleteUser()
        }*/

        return view


    }

    private fun updateItem(){
        val nomeRicetta = binding.updateRecipeNameEt.text.toString()
        val durata = Integer.parseInt(binding.updateDurationEt.text.toString())
        val descrizione = binding.updateRecipeDescriptionEt.text.toString()

        val posLivello = binding.updateRecipeLevelEt.lastVisiblePosition
        val posCategoria = binding.updateRecipeCategoryEt.lastVisiblePosition

        if(posLivello == 0 || posCategoria == 0) {
            Toast.makeText(requireContext(), "Please fill put all fields.", Toast.LENGTH_LONG).show()
            return
        }

        val livello = binding.updateRecipeLevelEt.selectedItem.toString()
        val categoria = binding.updateRecipeCategoryEt.selectedItem.toString()

        if(inputCheck(nomeRicetta, binding.updateDurationEt.text, livello, categoria, descrizione)){
            //Create recipe Object
            val testList = listOf("test1", "test2")
            val updatedRecipe = Ricetta(args.currentRecipe.nomeRicetta, durata, livello, categoria, descrizione, System.currentTimeMillis(), args.currentRecipe.ultimaEsecuzione, args.currentRecipe.count, testList)
            //Update Current Recipe
            mRecipeViewModel.aggiornaRicetta(updatedRecipe)
            Toast.makeText(requireContext(), "Updated Successfully!", Toast.LENGTH_SHORT).show()
            //Navigate Back
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }else{
            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun inputCheck(nomeRicetta: String, durata: Editable, livello: String, categoria: String, descrizione: String): Boolean{
        return !(TextUtils.isEmpty(nomeRicetta) && durata.isEmpty() && TextUtils.isEmpty(livello) && TextUtils.isEmpty(categoria) && TextUtils.isEmpty(descrizione))
    }


    private fun deleteRecipe() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes"){_, _->
            mRecipeViewModel.eliminaRicetta(args.currentRecipe)
            Toast.makeText(requireContext(), "Successfully removed: ${args.currentRecipe.nomeRicetta}", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        builder.setNegativeButton("No"){_, _-> }
        builder.setTitle("Delete ${args.currentRecipe.nomeRicetta}?")
        builder.setMessage("Are you sure you want to delete ${args.currentRecipe.nomeRicetta}")
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}*/