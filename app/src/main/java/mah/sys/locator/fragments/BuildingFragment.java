package mah.sys.locator.fragments;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import mah.sys.locator.FragmentCommunicator;
import mah.sys.locator.R;
import mah.sys.locator.ZoomableImageView;

/**
 * Created by Alex on 04-Apr-16.
 */
public class BuildingFragment extends Fragment {

    // Skapa variabler
    private ZoomableImageView mapView;
    private BuildingFragmentCommunicator callback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.w("Test", "View Infalated");
        return inflater.inflate(R.layout.building_layout, container, false);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        try {
            callback = (BuildingFragmentCommunicator) getActivity();
        } catch (ClassCastException e) {
            Log.w("Test", getActivity().toString() + " måste ärva BuildingFragmentCommunicator");
        }

        mapView = (ZoomableImageView) getView().findViewById(R.id.imageViewMap);
        Bitmap image = callback.getOverheadMap();
        mapView.setImageBitmap(image);
        String
            buildingName = callback.getBuildingName(),
            topText = getResources().getString(R.string.guide_building_top),
            bottomText = getResources().getString(R.string.guide_building_bottom) + " " +  buildingName;
        callback.setIntructions(topText,bottomText);
        Log.w("Test", "View State Restored");
    }

    public interface BuildingFragmentCommunicator extends FragmentCommunicator {
        Bitmap getOverheadMap();
        String getBuildingName();
    }
}
