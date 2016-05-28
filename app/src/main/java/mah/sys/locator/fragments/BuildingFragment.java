package mah.sys.locator.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import mah.sys.locator.FragmentCommunicator;
import mah.sys.locator.R;

/**
 * Created by Alex on 04-Apr-16.
 */
public class BuildingFragment extends Fragment {

    // Google Maps
    private MapView mapView;
    private GoogleMap googleMap;

    // Callback
    private BuildingFragmentCommunicator callback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_building, container, false);

        // Hämta callback.
        try {
            callback = (BuildingFragmentCommunicator) getActivity();
        } catch (ClassCastException e) {
            Log.w("Exception", getActivity().toString() + " måste ärva BuildingFragmentCommunicator");
        }

        callback.activateForwardButton();

        // Hämta view
        mapView = (MapView)v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        // Starta Maps.
        try{
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            Log.w("Exception", "Map Init Error: " + e.getMessage());
        }

        // Google Maps initiering.
        googleMap = mapView.getMap();

        // Plats info.
        double latitude = callback.getLatitude();
        double longitude = callback.getLongitude();

        // Skapa markör
        MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude,longitude)).title("Hello Maps");
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

        // Lägg till markör på karta.
        googleMap.addMarker(marker);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude,longitude)).zoom(17).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Sätt instruktioner.
        String
                buildingName = callback.getBuildingName(),
                topText = getResources().getString(R.string.guide_building_top),
                bottomText = getResources().getString(R.string.guide_building_bottom) + " " +  buildingName;
        callback.setInstructions(topText, bottomText);

        return v;
    }

    /**
     * Kommunikation med activity.
     */
    public interface BuildingFragmentCommunicator extends FragmentCommunicator {
        String getBuildingName();
        double getLongitude();
        double getLatitude();
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
