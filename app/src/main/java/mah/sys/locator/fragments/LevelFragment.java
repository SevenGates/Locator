package mah.sys.locator.fragments;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import mah.sys.locator.FragmentCommunicator;
import mah.sys.locator.R;

/**
 * Created by Alex on 20-Apr-16.
 */
public class LevelFragment extends Fragment {

    // Callback
    private LevelFragmentCommunicator callback;

    // Variabler
    private int goalFloor;

    // Views
    private TextView txtLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_level, container, false);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        // Hämta callback.
        try {
            callback = (LevelFragmentCommunicator) getActivity();
        } catch (ClassCastException e) {
            Log.w("Test", getActivity().toString() + " måste ärva LevelFragmentCommunicator");
        }

        // Hitta view.
        txtLevel = (TextView)getView().findViewById(R.id.txtLevel);

        // Hämta variabler.
        goalFloor = callback.getGoalFloor();

        // Sätt text.
        txtLevel.setText(Integer.toString(goalFloor));

        // Sätt instruktioner.
        String
                topText = getResources().getString(R.string.guide_level_top),
                bottomText = getResources().getString(R.string.guide_level_bottom) + " " + Integer.toString(goalFloor);
        callback.setInstructions(topText, bottomText);
    }

    /**
     * Kommunikation med activity.
     */
    public interface LevelFragmentCommunicator extends FragmentCommunicator {
        int getGoalFloor();
    }

}
