package mah.sys.locator.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;

import mah.sys.locator.MapActivity;
import mah.sys.locator.R;

/**
 * Created by Alex on 04-Apr-16.
 */
public class BuildingFragment extends Fragment {

    // Skapa variabler
    private ImageView mapView;
    private Activity activity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.w("Test", "View Infalated");
        return inflater.inflate(R.layout.building_layout,container,false);
    }

    @Override
    public void onViewStateRestored (Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.w("Test", "ViewStateRestored");
        mapView = (ImageView)getView().findViewById(R.id.imageViewMap);
        activity = getActivity();
    }

    public interface BuildingFragmentCommunicator {
        Bitmap fetchOverheadMap();
    }
}
