package com.xeniac.warrantyroster.mainactivity.warrantiesfragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.xeniac.warrantyroster.R;
import com.xeniac.warrantyroster.databinding.FragmentWarrantiesBinding;

public class WarrantiesFragment extends Fragment implements WarrantyListClickInterface {

    private FragmentWarrantiesBinding warrantiesBinding;
    private View view;
    private Activity activity;
    private Context context;
    private NavController navController;

    public WarrantiesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        warrantiesBinding = FragmentWarrantiesBinding.inflate(inflater, container, false);
        view = warrantiesBinding.getRoot();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        warrantiesBinding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        context = getContext();
        navController = Navigation.findNavController(view);

        boolean isListEmpty = false;
        //Todo can use databinding
        if (isListEmpty) {
            warrantiesBinding.searchWarranties.setVisibility(View.GONE);
            warrantiesBinding.rvWarranties.setVisibility(View.GONE);
            warrantiesBinding.groupWarrantiesEmptyList.setVisibility(View.VISIBLE);
        } else {
            warrantiesBinding.searchWarranties.setVisibility(View.VISIBLE);
            warrantiesBinding.groupWarrantiesEmptyList.setVisibility(View.GONE);
            warrantiesBinding.rvWarranties.setVisibility(View.VISIBLE);
            showWarrantyList();
            search();
        }
    }

    private void showWarrantyList() {
        WarrantyAdapter warrantyAdapter = new WarrantyAdapter(context, WarrantyDataProvider.warrantyList, this);
        warrantiesBinding.rvWarranties.setAdapter(warrantyAdapter);
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(context, WarrantyDataProvider.warrantyList.get(position).getTitle() + " clicked.",
                Toast.LENGTH_SHORT).show();
    }

    private void search() {
        warrantiesBinding.searchWarranties.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    warrantiesBinding.toolbarWarranties.setTitle(null);
                } else {
                    warrantiesBinding.toolbarWarranties.setTitle(context.getResources().getString(R.string.warranties_text_title));
                }
            }
        });

        warrantiesBinding.searchWarranties.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    Toast.makeText(context, "onQueryTextSubmit", Toast.LENGTH_SHORT).show();
                    warrantiesBinding.searchWarranties.onActionViewCollapsed();
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
}