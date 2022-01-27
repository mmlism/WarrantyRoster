package com.xeniac.warrantyroster_manager.ui.main.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xeniac.warrantyroster_manager.R
import com.xeniac.warrantyroster_manager.ui.main.adapters.WarrantyAdapter
import com.xeniac.warrantyroster_manager.ui.main.adapters.WarrantyListClickInterface
import com.xeniac.warrantyroster_manager.databinding.FragmentWarrantiesBinding
import com.xeniac.warrantyroster_manager.db.WarrantyRosterDatabase
import com.xeniac.warrantyroster_manager.models.Category
import com.xeniac.warrantyroster_manager.ui.main.MainActivity
import com.xeniac.warrantyroster_manager.models.ListItemType
import com.xeniac.warrantyroster_manager.models.Warranty
import com.xeniac.warrantyroster_manager.utils.Constants.CATEGORIES_ICON
import com.xeniac.warrantyroster_manager.utils.Constants.CATEGORIES_TITLE
import com.xeniac.warrantyroster_manager.utils.Constants.COLLECTION_CATEGORIES
import com.xeniac.warrantyroster_manager.utils.Constants.COLLECTION_WARRANTIES
import com.xeniac.warrantyroster_manager.utils.Constants.PREFERENCE_DB_SEED
import com.xeniac.warrantyroster_manager.utils.Constants.PREFERENCE_EN_US_KEY
import com.xeniac.warrantyroster_manager.utils.Constants.TAPSELL_KEY
import com.xeniac.warrantyroster_manager.utils.Constants.WARRANTIES_BRAND
import com.xeniac.warrantyroster_manager.utils.Constants.WARRANTIES_CATEGORY_ID
import com.xeniac.warrantyroster_manager.utils.Constants.WARRANTIES_DESCRIPTION
import com.xeniac.warrantyroster_manager.utils.Constants.WARRANTIES_EXPIRY_DATE
import com.xeniac.warrantyroster_manager.utils.Constants.WARRANTIES_MODEL
import com.xeniac.warrantyroster_manager.utils.Constants.WARRANTIES_SERIAL_NUMBER
import com.xeniac.warrantyroster_manager.utils.Constants.WARRANTIES_STARTING_DATE
import com.xeniac.warrantyroster_manager.utils.Constants.WARRANTIES_TITLE
import com.xeniac.warrantyroster_manager.utils.Constants.WARRANTIES_UUID
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusInitListener
import ir.tapsell.plus.model.AdNetworkError
import ir.tapsell.plus.model.AdNetworks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WarrantiesFragment : Fragment(R.layout.fragment_warranties), WarrantyListClickInterface {

    private var _binding: FragmentWarrantiesBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    private val categoriesCollectionRef = Firebase.firestore
        .collection(COLLECTION_CATEGORIES)
    private lateinit var database: WarrantyRosterDatabase

    private val warrantiesCollectionRef = Firebase.firestore
        .collection(COLLECTION_WARRANTIES)
    private var warrantiesQuery: ListenerRegistration? = null

    private val TAG = "WarrantiesFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWarrantiesBinding.bind(view)
        navController = Navigation.findNavController(view)
        database = WarrantyRosterDatabase(requireContext())
        (requireContext() as MainActivity).showNavBar()

        adInit()
        seedCategories()
        getWarrantiesListFromFirestore()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        warrantiesQuery?.remove()
        _binding = null
    }

    private fun adInit() = TapsellPlus.initialize(
        requireContext(), TAPSELL_KEY, object : TapsellPlusInitListener {
            override fun onInitializeSuccess(adNetworks: AdNetworks?) {
                Log.i(TAG, "onInitializeSuccess: ${adNetworks?.name}")
            }

            override fun onInitializeFailed(
                adNetworks: AdNetworks?, adNetworkError: AdNetworkError?
            ) {
                Log.e(
                    TAG,
                    "onInitializeFailed: ${adNetworks?.name}, error: ${adNetworkError?.errorMessage}"
                )
            }
        })

    private fun seedCategories() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val seedPrefs = requireContext()
                .getSharedPreferences(PREFERENCE_DB_SEED, Context.MODE_PRIVATE)
            val isEnUsSeeded = seedPrefs.getBoolean(PREFERENCE_EN_US_KEY, false)

            //TODO add isFaIRSeeded after adding persian
            if (!isEnUsSeeded) {
                database.getCategoryDao().deleteAllCategories()
                val categoriesQuery = categoriesCollectionRef
                    .orderBy(CATEGORIES_TITLE, Query.Direction.ASCENDING)
                    .get().await()
                Log.i(TAG, "Categories successfully retrieved.")

                val categoriesList = mutableListOf<Category>()
                for (document in categoriesQuery.documents) {
                    @Suppress("UNCHECKED_CAST")
                    document?.let {
                        val id = it.id
                        val title = it.get(CATEGORIES_TITLE) as Map<String, String>
                        val icon = it.get(CATEGORIES_ICON).toString()
                        categoriesList.add(Category(id, title, icon))
                    }
                }
                database.getCategoryDao().insertAllCategories(categoriesList)

                val itemCount = database.getCategoryDao().countItems()
                if (itemCount == 21) {
                    Log.i(TAG, "categories successfully seeded to DB.")
                    requireContext().getSharedPreferences(
                        PREFERENCE_DB_SEED, Context.MODE_PRIVATE
                    ).edit().apply {
                        putBoolean(PREFERENCE_EN_US_KEY, true)
                        apply()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
        }
    }

    private fun getWarrantiesListFromFirestore() {
        showLoadingAnimation()

        warrantiesQuery = warrantiesCollectionRef
            .whereEqualTo(WARRANTIES_UUID, Firebase.auth.currentUser?.uid)
            .orderBy(WARRANTIES_TITLE, Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                error?.let {
                    Log.e(TAG, "Exception: ${it.message}")
                    binding.tvNetworkError.text =
                        requireContext().getString(R.string.network_error_connection)
                    showNetworkError()
                }

                value?.let {
                    Log.i(TAG, "Warranties List successfully retrieved.")
                    hideLoadingAnimation()

                    if (it.documents.size == 0) {
                        showWarrantiesEmptyList()
                    } else {
                        val warrantiesList = mutableListOf<Warranty>()
                        var adIndex = 5

                        for (document in it.documents) {
                            val warranty = Warranty(
                                document.id,
                                document.get(WARRANTIES_TITLE).toString(),
                                document.get(WARRANTIES_BRAND).toString(),
                                document.get(WARRANTIES_MODEL).toString(),
                                document.get(WARRANTIES_SERIAL_NUMBER).toString(),
                                document.get(WARRANTIES_STARTING_DATE).toString(),
                                document.get(WARRANTIES_EXPIRY_DATE).toString(),
                                document.get(WARRANTIES_DESCRIPTION).toString(),
                                document.get(WARRANTIES_CATEGORY_ID).toString(),
                                ListItemType.WARRANTY
                            )
                            warrantiesList.add(warranty)

                            if (warrantiesList.size == adIndex) {
                                adIndex += 6
                                val nativeAd = Warranty(
                                    null, null, null, null,
                                    null, null, null,
                                    null, null, ListItemType.AD
                                )
                                warrantiesList.add(nativeAd)
                            }
                        }
                        showWarrantiesList(warrantiesList)
                    }
                }
            }
    }

    private fun showNetworkError() {
        hideLoadingAnimation()
        binding.groupEmptyList.visibility = GONE
        binding.rv.visibility = GONE
        binding.groupNetwork.visibility = VISIBLE
        retryNetworkBtn()
    }

    private fun retryNetworkBtn() = binding.btnNetworkRetry.setOnClickListener {
        getWarrantiesListFromFirestore()
    }

    private fun showLoadingAnimation() {
        binding.svWarranties.visibility = GONE
        binding.groupNetwork.visibility = GONE
        binding.groupEmptyList.visibility = GONE
        binding.rv.visibility = GONE
        binding.cpi.visibility = VISIBLE
    }

    private fun hideLoadingAnimation() {
        binding.cpi.visibility = GONE
    }

    private fun showWarrantiesEmptyList() {
        binding.svWarranties.visibility = GONE
        binding.groupNetwork.visibility = GONE
        binding.rv.visibility = GONE
        binding.groupEmptyList.visibility = VISIBLE
    }

    private fun showWarrantiesList(warrantiesList: MutableList<Warranty>) {
        binding.groupNetwork.visibility = GONE
        binding.groupEmptyList.visibility = GONE
        binding.rv.visibility = VISIBLE

        val warrantyAdapter = WarrantyAdapter(
            requireActivity(), requireContext(), database,
            warrantiesList, this
        )
        binding.rv.adapter = warrantyAdapter

        //TODO remove comment after adding search function
//        searchWarrantiesList();
    }

    override fun onItemClick(warranty: Warranty, daysUntilExpiry: Long) {
        val action = WarrantiesFragmentDirections
            .actionWarrantiesFragmentToWarrantyDetailsFragment(warranty, daysUntilExpiry)
        navController.navigate(action)
    }

    /*
    private fun searchWarrantiesList() {
        binding.svWarranties.setVisibility(VISIBLE);

        binding.svWarranties.setOnQueryTextFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                warrantiesBinding.toolbarWarranties.setTitle(null);
            } else {
                warrantiesBinding.toolbarWarranties.setTitle(
                    context.getResources().getString(R.string.warranties_text_title)
                );
            }
        });

        warrantiesBinding.svWarranties.setOnQueryTextListener(new SearchView . OnQueryTextListener () {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    Toast.makeText(context, "onQueryTextSubmit", Toast.LENGTH_SHORT).show();
                    warrantiesBinding.svWarranties.onActionViewCollapsed();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    Toast.makeText(context, "Input: " + newText, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
     */
}