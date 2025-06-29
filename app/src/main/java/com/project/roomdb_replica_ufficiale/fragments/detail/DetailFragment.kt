package com.project.roomdb_replica_ufficiale.fragments.detail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.project.roomdb_replica_ufficiale.R
import com.project.roomdb_replica_ufficiale.data.ricetta.RicettaViewModel
import com.project.roomdb_replica_ufficiale.databinding.FragmentDetailBinding
import com.project.roomdb_replica_ufficiale.fragments.update.UpdateFragmentArgs


class DetailFragment : Fragment() {

    //private val args by navArgs<DetailFragmentArgs>()
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var mRicettaViewModel: RicettaViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        mRicettaViewModel = ViewModelProvider(this)[RicettaViewModel::class.java]

        val args = DetailFragmentArgs.fromBundle(requireArguments())
        val nomeRicetta = args.currentRecipe.nomeRicetta
        val idRicetta = args.currentRecipe.idRicetta

        mRicettaViewModel.getRicettaConIstruzioni(idRicetta).observe(viewLifecycleOwner) { dati ->
            binding.numeroCompletamenti.text= "${dati.ricetta.count} volte"
            binding.nomeTextView.text = dati.ricetta.nomeRicetta
            binding.descrizioneContentTextView.text = dati.ricetta.descrizione
            binding.durataContentTextView.text = "${dati.ricetta.durata} min"
            binding.categoriaContentTextView.text = dati.ricetta.categoria
            binding.difficoltaContentTextView.text = dati.ricetta.livello

            val steps = dati.istruzioni.sortedBy { it.numero }
            binding.stepListLayout.removeAllViews()
            steps.forEach {
                val stepView = TextView(requireContext()).apply {
                    text = "${it.numero}. ${it.descrizione}"
                    textSize = 16f
                    setPadding(0, 8, 0, 8)
                }
                binding.stepListLayout.addView(stepView)
            }


            val allergeniList = dati.ricetta.allergeni ?: emptyList()
            binding.allergeniLayout.removeAllViews()

            if (allergeniList.isNotEmpty()) {
                allergeniList.forEach {
                    val allergeneView = TextView(requireContext()).apply {
                        text = "- $it"
                        textSize = 16f
                        setPadding(0, 4, 0, 4)
                    }
                    binding.allergeniLayout.addView(allergeneView)
                }
            } else {
                val noAllergeniView = TextView(requireContext()).apply {
                    text = "Nessun allergene specificato"
                    setTextColor(Color.GRAY)
                }
                binding.allergeniLayout.addView(noAllergeniView)
            }
        }

        mRicettaViewModel.getIngredientiConQuantitaPerRicetta(idRicetta).observe(viewLifecycleOwner) { ingredienti ->
            binding.ingredientListLayout.removeAllViews() // Assicurati che il layout esista nel tuo XML

            ingredienti.forEach {
                val ingredienteView = TextView(requireContext()).apply {
                    text = "- ${it.nomeIngrediente}: ${it.quantita}"
                    textSize = 16f
                    setPadding(0, 8, 0, 8)
                }
                binding.ingredientListLayout.addView(ingredienteView)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}