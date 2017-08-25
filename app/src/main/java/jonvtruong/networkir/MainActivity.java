package jonvtruong.networkir;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {
    final int PORT = 8888;
    final int FREQUENCY = 38000; // carrier frequency for remote
    final int[] POWER_CODE = {
            //  on      off
            9220,4580,
            650,520,
            630,530,
            630,510,
            630,1690,
            650,510,
            630,530,
            620,530,
            620,530,
            620,1690,
            640,540,
            620,1660,
            640,520,
            630,1700,
            630,1680,
            640,1710,
            630,1680,
            650,1690,
            640,530,
            610,540,
            620,530,
            610,1700,
            640,540,
            610,520,
            630,530,
            630,530,
            620,1680,
            650,1690,
            640,1690,
            650,520,
            630,1680,
            660,1680,
            640,1690,
            640,42530
    };

    AndroidWebServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        server = new AndroidWebServer(PORT);
    }

    /**
     * Called when the user taps the power button
     */
    public void powerButton(View view) {
        Log.d("console","button pressed");
        new TogglePower().execute();
    }

    /** Asynchronous task to send power command via IR **/
    private class TogglePower extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... arg) {
            ConsumerIrManager ir = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);
            //checks if device has ir emitter before running
            if(ir.hasIrEmitter()){
                Log.d("console","has ir");

                try {
                    ir.transmit(FREQUENCY, POWER_CODE);
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("console", "Exception: " + e.toString());
                }
            }
            return "Power Toggled";
        }

        @Override
        protected void onPostExecute(String result) { //after command sent shows toast
            Toast toast = Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private class AndroidWebServer extends NanoHTTPD {

        private AndroidWebServer(int port){
            super(port);
            try {
                start();
            }catch(IOException e){
                e.printStackTrace();
                Log.d("console",e.toString());
            }
        }

        @Override
        public Response serve(IHTTPSession session) {
            String msg = "<html><head><link rel=\"icon\" href=\"data:;base64,=\"></head><body>\n";
         //   Method method = session.getMethod();
            Map<String, String> parms = session.getParms();
            Log.d("console","get received");

            if (parms.get("cmd").equals("toggle")) {
                    new TogglePower().execute();
            }

            return newFixedLengthResponse( msg + "</body></html>\n" );
        }
    }
}


