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

    private LevelFragmentCommunicator callback;
    private int
        goalFloor,
        maxFloors;
    private TextView txtLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_level, container, false);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        try {
            callback = (LevelFragmentCommunicator) getActivity();
        } catch (ClassCastException e) {
            Log.w("Test", getActivity().toString() + " måste ärva LevelFragmentCommunicator");
        }

        txtLevel = (TextView)getView().findViewById(R.id.txtLevel);

        goalFloor = callback.getGoalFloor();
        maxFloors = callback.getMaxFloors();

        txtLevel.setText(goalFloor + "/" + maxFloors);

        String
                topText = getResources().getString(R.string.guide_level_top),
                bottomText = getResources().getString(R.string.guide_level_bottom) + " " + Integer.toString(goalFloor);
        callback.setIntructions(topText,bottomText);
    }

    public interface LevelFragmentCommunicator extends FragmentCommunicator {
        int getMaxFloors();
        int getGoalFloor();
    }

}
