package mah.sys.locator;

import android.graphics.Bitmap;

/**
 * Created by Alex on 14-Apr-16.
 */
public interface FragmentCommunicator {

    void setInstructions(String top, String bottom);
    void activateForwardButton();
    void deactivateForwardButton();
}
