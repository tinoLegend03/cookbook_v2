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


class HomeFragment : Fragment() {

    private lateinit var mRecipeViewModel: RicettaViewModel
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.cardAddRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addFragment)
        }

        binding.cardMyRecipes.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_listFragment)
        }

        //Recyclerview
        val adapter = RecentRecipesAdapter()
        val recyclerView = _binding!!.recyclerViewRecentlyMade
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        //UserViewModel
        mRecipeViewModel = ViewModelProvider(this).get(RicettaViewModel::class.java)
        mRecipeViewModel.ultime10Ricette.observe(viewLifecycleOwner, Observer { ricetta ->
            adapter.setData(ricetta)
        })

        // Solo da API 33 in su è richiesto il permesso per le notifiche
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission (requireContext(),
                    POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission()
            }
        } else {
            Toast.makeText(context, "No need permission", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){ isGranted: Boolean ->
        if(isGranted){
            Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()

        }

    }

    private fun requestNotificationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(POST_NOTIFICATIONS)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "" // Nasconde il titolo del Fragment
    }

}