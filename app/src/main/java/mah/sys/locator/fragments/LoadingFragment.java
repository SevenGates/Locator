package mah.sys.locator.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import mah.sys.locator.R;


public class LoadingFragment extends Fragment {

    private ProgressBar spinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_loading,container,false);
    }

    @Override
    public void onViewStateRestored (Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        spinner = (ProgressBar)getView().findViewById(R.id.loading_spin);
        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //TODO: Detta måste kunna lösas bättre???
        spinner.setVisibility(View.GONE);
    }
}
