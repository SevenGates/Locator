package mah.sys.locator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class MapActivity extends AppCompatActivity {

    private ImageView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = (ImageView)findViewById(R.id.imageViewMap);
        Intent intent = getIntent();
        boolean isRoomSearch = intent.getBooleanExtra("isRoomSearch", true);
        String searchTerm = intent.getStringExtra("searchTerm");

        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Socket socket;
                    socket = new Socket("192.168.1.164", 8080);
                    PrintWriter output = new PrintWriter(socket.getOutputStream());
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    output.write("Test from Android");
                    output.flush();
                    Log.w("Test", "Message Sent");
                    Log.w("Text", String.valueOf(input.available()));
                    byte[] bytes = new byte[1000000];
                    input.read(bytes);
                    Log.w("Text", String.valueOf(input.available()));
                    Log.w("Test", "Read Bytes");
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    final Bitmap map = bitmap; // LOL
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mapView.setImageBitmap(map);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
