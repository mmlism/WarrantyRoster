package com.xeniac.warrantyroster.mainactivity.morefragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.xeniac.warrantyroster.Constants;
import com.xeniac.warrantyroster.databinding.FragmentMoreBinding;
import com.xeniac.warrantyroster.landingactivity.LandingActivity;
import com.xeniac.warrantyroster.mainactivity.MainActivity;

public class MoreFragment extends Fragment {

    private FragmentMoreBinding moreBinding;
    private View view;
    private Activity activity;
    private Context context;
    private NavController navController;

    public MoreFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        moreBinding = FragmentMoreBinding.inflate(inflater, container, false);
        view = moreBinding.getRoot();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        moreBinding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        context = getContext();
        navController = Navigation.findNavController(view);
        ((MainActivity) context).showNavBar();

        logoutOnClick();
    }

    private void logoutOnClick() {
        moreBinding.btnMoreLogout.setOnClickListener(view -> {
            SharedPreferences.Editor editor = context
                    .getSharedPreferences(Constants.PREFERENCE_LOGIN, Context.MODE_PRIVATE).edit();
            editor.remove(Constants.PREFERENCE_USER_TOKEN_KEY).apply();
            editor.remove(Constants.PREFERENCE_IS_LOGGED_IN_KEY).apply();

            startActivity(new Intent(context, LandingActivity.class));
            activity.finish();
        });
    }
}