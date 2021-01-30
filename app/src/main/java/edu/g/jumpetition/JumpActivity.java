package edu.g.jumpetition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;

public class JumpActivity extends AppCompatActivity implements SensorEventListener {

    private int mode;

    // Views
    private TextView countDownTV, jumpCountTV;

    // Timer: 2 minute 120000L
    private Long startTime = 120000L, currentTime = startTime;
    private boolean timerFinish = false;

    // Accelerometer
    SensorManager mSensorManager;
    Sensor mAccelerometer;
    private float previousX;
    private float previousY;
    private float previousZ;
    private boolean inJump = true, isFirst = true;

    private Handler handler;
    private Communicate communicate;

    private boolean sentMsg = false;
    private boolean receivedMsg = false;
    private int theirScore;
    private String opponentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jump);

        handler = messageHandler;

        countDownTV = findViewById(R.id.count_down_text_view);
        jumpCountTV = findViewById(R.id.jump_count_text_view);

        mode = getIntent().getIntExtra("Mode", 0);
        opponentName = getIntent().getStringExtra("opponentName");

        // setup accelerometer
        // Accelerometer
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /* register the receiver on resume
     * */
    @Override
    public void onResume() {
        super.onResume();
    }

    /* unregister receiver on pause
     * */
    @Override
    public void onPause() {
        super.onPause();
    }

    private void stopCount() throws InterruptedException {
        mSensorManager.unregisterListener(this, mAccelerometer);
        if (mode == MainActivity.COMPETITION_MODE){
            sendMsg msg = new sendMsg(jumpCountTV.getText().toString());
            msg.start();
            msg.join();
            System.out.println("I got back from send Msg");
            sentMsg = true;
            if (receivedMsg){
                DisplayResults();
            }
        }
        else if (mode == MainActivity.PRACTICE_MODE){
            DisplayResults();
        }
    }

    @Override
    protected void onStart()
    {
        // start up communicate
        if (MainActivity.COMPETITION_MODE == mode){
            communicate = new Communicate();
            communicate.start();
        }

        // start the timer
        startTimer();
        super.onStart();
    }



    /*-- Timer ----------------------------------------------------------------------------------*/

    /*  Function starts the timer
    * */
    private void startTimer(){
        // decrement count down with every tick
        CountDownTimer mTimer = new CountDownTimer(currentTime, 1000) {

            // decrement count down with every tick
            @Override
            public void onTick(long timeLeft) {
                currentTime = timeLeft;
                int min = (int) (currentTime / 1000) / 60;
                int sec = (int) (currentTime / 1000) % 60;
                String newText = String.format(Locale.getDefault(), "%02d:%02d", min, sec);
                countDownTV.setText(newText);
            }

            // send the score to the opponent / go to next activity
            @Override
            public void onFinish() {
                try {
                    stopCount();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.cancel();
            }

        }.start();
    }

    /*-- Accelerometer --------------------------------------------------------------------------*/

    /*  Accelerometer sensor event has occurred
    * */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent != null && sensorEvent.accuracy >= SENSOR_STATUS_ACCURACY_MEDIUM){

            // set the newly detected values to current
            float currentZ = sensorEvent.values[2];
            float currentY = sensorEvent.values[0];
            float currentX = sensorEvent.values[1];

            if (!isFirst){
                // find the amount of change in distance by calculating delta
                float deltaZ = Math.abs(previousZ - currentZ);
                float deltaY = Math.abs(previousY - currentY);
                float deltaX = Math.abs(previousX - currentX);

                // only increment if there is a big enough delta
                if (deltaX > 3 || deltaY > 3 || deltaZ > 3) {
                    // if previous < current we are heading upwards into a jump
                    if (previousX < currentX || previousY < currentY || previousZ < currentZ) {
                        if (inJump) {
                            incrementCount();
                            inJump = false;  // the counter will increment for an upward motion if true
                        }
                    }
                }

                else if(deltaX > 2.8 || deltaY > 2.8 || deltaZ > 2.8){
                    // we are going down if previous > current
                    if (previousX >= currentX || previousY >= currentY || previousZ >= currentZ) {
                        if (!inJump) {
                            inJump = true;
                        }
                    }
                }
            }

            // current data is previous data now that we are done using it
            previousZ = currentZ;
            previousY = currentY;
            previousX = currentX;
            isFirst = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /* increment the count text view by 1*/
    private void incrementCount(){
        String newText = Integer.toString(Integer.parseInt(jumpCountTV.getText().toString()) + 1);
        jumpCountTV.setText(newText);
    }

    /*-- Networking -----------------------------------------------------------------------------*/


    /* Thread Handles reception and sending of messages
     * */
    public class Communicate extends Thread{
        public static final int MESSAGE_SCORE = 2;

        @Override
        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;
            boolean gotMsg = false;
            while(mySocket.getSocket() != null){
                if (!gotMsg){
                try{
                    System.out.println("hoping for msg");
                    bytes = mySocket.getInputStream().read(buffer);
                    if(bytes > 0){
                        handler.obtainMessage(MESSAGE_SCORE, bytes, -1, buffer).sendToTarget();
                        gotMsg = true;
                    }
                } catch(IOException e){
                    System.out.println("Somethings wrong");
                    e.printStackTrace();
                } }
            }
        }

        public void write(byte[] bytes) throws IOException {
            mySocket.getOutputStream().write(bytes);
            System.out.println("\nI got to write\n");
        }
    }

    private class sendMsg extends Thread{
        String message;

        public sendMsg(String message){
            this.message = message;
        }

        @Override
        public void run(){
            try {
                while(communicate == null){
                    Thread.sleep(1);
                }
                communicate.write(message.getBytes());
                System.out.println("\nI got to send message\n");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /* Handles message received
    * */
    Handler messageHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            System.out.println("\nI got to handle message\n");
            switch (message.what){
                case Communicate.MESSAGE_SCORE:
                    byte[] readBuffer = (byte[]) message.obj;
                    String msg = new String(readBuffer, 0, message.arg1);
                    calculateWinner(msg);
                    break;
            }
            return true;
        }
    });

    /* Calculate the winner
    * */
    private void calculateWinner(final String opponentScore) {
        Handler nameHandler = new Handler(Looper.getMainLooper());
        nameHandler.post(new Runnable() {
            @Override
            public void run() {
                theirScore = Integer.parseInt(opponentScore);
                receivedMsg = true;
                // start the next activity if our score was sent
                if (sentMsg){
                    DisplayResults();
                }
            }
        });
    }

    private void DisplayResults(){
        System.out.println("I got to the end");
        Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
        intent.putExtra("Mode", mode);
        intent.putExtra("myScore", Integer.parseInt(jumpCountTV.getText().toString()));
        if (mode == MainActivity.COMPETITION_MODE){
            intent.putExtra("opponentScore", theirScore);
            intent.putExtra("opponentName", opponentName);
            // communicate.interrupt();
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

        /*-------------------------------------------------------------------------------------------*/
}