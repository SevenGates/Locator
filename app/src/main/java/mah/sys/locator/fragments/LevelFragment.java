package mah.sys.locator.fragments;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mah.sys.locator.FragmentCommunicator;
import mah.sys.locator.NoDefaultSpinner;
import mah.sys.locator.R;

/**
 * Created by Alex on 20-Apr-16.
 */
public class LevelFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    // Callback
    private LevelFragmentCommunicator callback;

    // Variabler
    private int goalFloor;

    // Views
    private TextView txtLevel;
    private NoDefaultSpinner sprPathSelector;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_level, container, false);

        // Hämta callback.
        try {
            callback = (LevelFragmentCommunicator) getActivity();
        } catch (ClassCastException e) {
            Log.w("Exception", getActivity().toString() + " måste ärva LevelFragmentCommunicator");
        }

        // Stäng av frammåt-knapp om inget valt.
        if(callback.getPathNbr() == -1)
            callback.deactivateForwardButton();

        // Hitta views.
        txtLevel = (TextView)v.findViewById(R.id.txtLevel);
        sprPathSelector = (NoDefaultSpinner)v.findViewById(R.id.sprPathSelectorLevel);
        sprPathSelector.setSaveEnabled(false);

        // Hämta variabler.
        goalFloor = callback.getGoalFloor();

        // Sätt text.
        txtLevel.setText(Integer.toString(goalFloor));

        // Hämta vägnamn.
        ArrayList<String> items = new ArrayList<>(Arrays.asList(callback.getPathNames()));

        // Instansiera Adapter till spinner.
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, items);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprPathSelector.setAdapter(spinnerArrayAdapter);
        sprPathSelector.setOnItemSelectedListener(this);

        if(callback.getPathNbr() != -1)
            sprPathSelector.setSelection(callback.getPathNbr());

        // Sätt instruktioner.
        String
                topText = getResources().getString(R.string.guide_level_top),
                bottomText = getResources().getString(R.string.guide_level_bottom) + " " + Integer.toString(goalFloor);
        callback.setInstructions(topText, bottomText);

        return v;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        callback.setPathNbr(position);
        callback.activateForwardButton();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.w("Log","Nothing");
    }

    /**
     * Kommunikation med activity.
     */
    public interface LevelFragmentCommunicator extends FragmentCommunicator {
        int getGoalFloor();
        void setPathNbr(int path);
        int getPathNbr();
        String[] getPathNames();
    }

}
