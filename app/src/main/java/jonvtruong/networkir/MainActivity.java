package jonvtruong.networkir;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.os.AsyncTask;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Calendar;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {
    private final int PORT = 8888;
    private final int FREQUENCY = 38000; // carrier frequency for remote
    private final int[] POWER_CODE = {
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
    private Date lastRun;
    private SimpleDateFormat sdf;
    private String last = "Never";
    private String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        server = new AndroidWebServer(PORT);
    }

    @Override
    protected void onResume(){
        super.onResume();
        server = new AndroidWebServer(PORT);
    }

    /**
     * Called when the user taps the power button
     */
    public void powerButton(View view) {
        Log.d("console","button pressed");
        new TogglePower().execute();
        lastRun = Calendar.getInstance().getTime();
        updateLast();
    }

    private void updateLast(){
        msg = "<html><head><link rel=\"icon\" href=\"data:;base64,=\"></head><body>\n";
        Log.d("console", "last run: " + last);

        msg += "<h1>Log</h1>\n";
        msg += "<p>Last run: " + last + "</p>\n";

        if(lastRun != null) {
            last = sdf.format(lastRun);
        }
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
            Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            if (vib.hasVibrator()) {
                vib.vibrate(100);
            }

            Toast toast = Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private class AndroidWebServer extends NanoHTTPD {
        private AndroidWebServer(int port){
            super(port);
            sdf = new SimpleDateFormat("EEE, MMM d, yyyy h:mm:ss a");
            try {
                start();
            }catch(IOException e){
                e.printStackTrace();
                Log.d("console",e.toString());
            }
        }

        @Override
        public Response serve(IHTTPSession session) {
            msg = "<html><head><link rel=\"icon\" href=\"data:;base64,=\"></head><body>\n";
            Map<String, String> parms = session.getParms();
            Log.d("console","get received");

            if (!parms.isEmpty()) { // if there is a parameter in the get request
                if (parms.get("cmd").equals("toggle")) { // if toggled, store lastrun into last, then set lastrun to current time
                    new TogglePower().execute();
                    lastRun = Calendar.getInstance().getTime();
                }
            }

            updateLast();

            return newFixedLengthResponse( msg + "</body></html>\n" );
        }
    }
}


