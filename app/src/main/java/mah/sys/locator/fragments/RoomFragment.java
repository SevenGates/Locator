package mah.sys.locator.fragments;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import mah.sys.locator.FragmentCommunicator;
import mah.sys.locator.R;

/**
 * Created by Alex on 14-Apr-16.
 */
public class RoomFragment extends Fragment {

    private ImageView imgViewRoomMap;
    private RoomFragmentCommunicator callback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_room, container, false);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        try {
            callback = (RoomFragmentCommunicator) getActivity();
        } catch (ClassCastException e) {
            Log.w("Test", getActivity().toString() + " måste ärva RoomFragmentCommunicator");
        }

        imgViewRoomMap = (ImageView) getView().findViewById(R.id.imgViewRoomMap);
        Bitmap image = callback.getFloorMap();

        imgViewRoomMap.setImageBitmap(image);
        String
                topText = getResources().getString(R.string.guide_room_top),
                bottomText = getResources().getString(R.string.guide_room_bottom);
        callback.setIntructions(topText,bottomText);
    }

    public interface RoomFragmentCommunicator extends FragmentCommunicator {
        Bitmap getFloorMap();
        int getRoomX();
        int getRoomY();
        int getDoorX();
        int getDoorY();
        int getCorridorX();
        int getCorridorY();
    }
}
