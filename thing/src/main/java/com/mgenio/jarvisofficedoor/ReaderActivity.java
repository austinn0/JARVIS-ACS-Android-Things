package com.mgenio.jarvisofficedoor;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mgenio.jarvisofficedoor.interfaces.WiegandInterface;
import com.mgenio.jarvisofficedoor.models.AccessKey;
import com.mgenio.jarvisofficedoor.models.BoomMusic;
import com.mgenio.jarvisofficedoor.models.Card;
import com.mgenio.jarvisofficedoor.models.Door;
import com.mgenio.jarvisofficedoor.models.Location;
import com.mgenio.jarvisofficedoor.models.Logs;
import com.mgenio.jarvisofficedoor.models.Pin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import zh.wang.android.yweathergetter4a.WeatherInfo;
import zh.wang.android.yweathergetter4a.YahooWeather;

public class ReaderActivity extends Activity implements WiegandInterface {
    private static final String TAG = "MainActivity";
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 5000;
    private static final String GPIO_PIN_RELAY_NAME = "BCM6";
    private Gpio mRelayGpio;

    private Handler mHandler = new Handler();

    private DatabaseReference mDatabase;
    private MediaPlayer mediaPlayer;
    private TextToSpeech tts;

    private Location location;

    private long lastTimeDoorOpened = 0;

    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PeripheralManagerService service = new PeripheralManagerService();

        try {
            mRelayGpio = service.openGpio(GPIO_PIN_RELAY_NAME);
            mRelayGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override public void onInit(int status) {

            }
        });

        tv = (TextView) findViewById(R.id.textview);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.jarvis_on);
        mediaPlayer.start();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        /**
         * This monitors changes on Location
         * Use Cases:
         * Card Provisioning
         */
        mDatabase.child("locations").child(getString(R.string.device_id)).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                location = dataSnapshot.getValue(Location.class);
                Log.d("ReaderActivity", "Mode: " + location.getMode());
            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("doors").orderByChild("location").equalTo(getString(R.string.device_id)).addChildEventListener(new ChildEventListener() {
            @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                long now = System.currentTimeMillis();
                //Log.d("ReaderActivity", "Time since last opened: " + (now - lastTimeDoorOpened));
                if ((now - lastTimeDoorOpened) == now) {
                    //first time
                    //Log.d("ReaderActivity", "First Time");
                    lastTimeDoorOpened = now;
                    return;
                } else if ((now - lastTimeDoorOpened) < 10000) {
                    //subsequent time less than a second
                    lastTimeDoorOpened = now;
                    return;
                } else {
                    lastTimeDoorOpened = now;
                }

                Door door = dataSnapshot.getValue(Door.class);
                final Logs log = new Logs(System.currentTimeMillis(), getString(R.string.device_id), door.getAccessKey(), door.getAccessKeyName());
                final String logKey = mDatabase.child("logs").push().getKey();
                mDatabase.child("logs").child(logKey).setValue(log);

                authorize(log, logKey, door.getAccessKey());
            }

            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });

        //initialize the Wiegand 26 protocol
        Wiegand wiegand = new Wiegand();
        wiegand.setLoginCallback(this);
        wiegand.begin();
    }

    private void getWeatherInfo(AccessKey accessKey) {
        YahooWeather mYahooWeather = YahooWeather.getInstance();
        mYahooWeather.setUnit(YahooWeather.UNIT.FAHRENHEIT);
        mYahooWeather.queryYahooWeatherByPlaceName(this, "Cleveland", new YahooWeatherInfoListener(accessKey));
    }

    @Override public void onKeypadWiegandRecieved(final String data) {
        final Logs log = new Logs(getString(R.string.device_id), data, System.currentTimeMillis());
        final String logKey = mDatabase.child("logs").push().getKey();
        mDatabase.child("logs").child(logKey).setValue(log);

        mDatabase.child("pins").orderByChild("location").startAt(getString(R.string.device_id)).endAt(getString(R.string.device_id)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                boolean foundPin = false;
                String pinAccessKey = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Pin pin = snapshot.getValue(Pin.class);
                    if (pin.getPin().equals(data)) {
                        foundPin = true;
                        pinAccessKey = pin.getAccessKey();
                        break;
                    }
                }

                if (foundPin && null != pinAccessKey) {
                    authorize(log, logKey, pinAccessKey);
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override public void onCardWiegandRecieved(final String facilityData, final String cardData) {
        if (location.getMode().equals("provisioning")) {
            Card provisionedCard = new Card(facilityData, cardData, null, getString(R.string.device_id), System.currentTimeMillis());
            mDatabase.child("locations").child(getString(R.string.device_id)).child("card-provisioning").push().setValue(provisionedCard);
        } else {
            final Logs log = new Logs(getString(R.string.device_id), facilityData, cardData, System.currentTimeMillis());
            final String logKey = mDatabase.child("logs").push().getKey();
            mDatabase.child("logs").child(logKey).setValue(log);

            mDatabase.child("cards").orderByChild("location").startAt(getString(R.string.device_id)).endAt(getString(R.string.device_id)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot dataSnapshot) {
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Card card = snapshot.getValue(Card.class);
                        if (card.getFacility().equals(facilityData) && card.getCard().equals(cardData)) {
                            authorize(log, logKey, card.getAccessKey());
                            break;
                        }
                    }
                }

                @Override public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * @param log
     * @param logKey
     * @param mAccessKey
     */
    private void authorize(final Logs log, final String logKey, final String mAccessKey) {
        mDatabase.child("access-keys").child(mAccessKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                long now = System.currentTimeMillis();
                AccessKey accessKey = dataSnapshot.getValue(AccessKey.class);
                accessKey.setKey(dataSnapshot.getKey());
                if (accessKey.isEnabled() && (accessKey.getExpirationDate() == -1 || accessKey.getExpirationDate() > now)) {
                    unlockDoor(accessKey);
                    log.setAuthorized(true);
                    sendNotification("Door unlocked by " + ((null == accessKey.getName()) ? dataSnapshot.getKey() : accessKey.getName()));
                } else {
                    mediaPlayer = MediaPlayer.create(ReaderActivity.this, R.raw.jarvis_authorized_not);
                    mediaPlayer.start();
                    log.setAuthorized(false);
                    sendNotification("Unauthorized access by " + ((null == accessKey.getName()) ? dataSnapshot.getKey() : accessKey.getName()));
                }

                //update log
                log.setAccessKey(dataSnapshot.getKey());
                mDatabase.child("logs").child(logKey).setValue(log);
            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * @param accessKey
     */
    private void unlockDoor(AccessKey accessKey) {
        tv.setText("Welcome " + accessKey.getName() + accessKey.getKey());
        Log.d("ReaderActivity", "Welcome " + accessKey.getName() + accessKey.getKey());
        open();

        //if accessKey.morningGreeting
        //query logs for first time
        if (false) {
            checkLogsForFirstMorningGreeting(accessKey);
        } else {
            playBoomMusic(accessKey.getKey());
        }
    }

    private void open() {
        try {
            Log.d("thing:MainActivity (onChildAdded)", "Unlocking Door...");
            mRelayGpio.setValue(true);
            mHandler.postDelayed(mOffRunnable, INTERVAL_BETWEEN_BLINKS_MS);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    /**
     *
     */
    private void sendNotification(String message) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put("message", message);

            jsonObject.put("to", "/topics/" + getString(R.string.device_id));
            jsonObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(
                "http://fcm.googleapis.com/fcm/send",
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(), response + "", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "key=" + getString(R.string.fcm_key));
                params.put("Content-Type", "application/json");
                return params;
            }
        };

        queue.add(req);
    }

    /**
     * @param accessKey
     */
    private void playBoomMusic(String accessKey) {
        mDatabase.child("boom-music").orderByChild("accessKey").startAt(accessKey).endAt(accessKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BoomMusic boomMusic = snapshot.getValue(BoomMusic.class);
                    try {
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(ReaderActivity.this, Uri.parse(boomMusic.getSpotifyUrl()));
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * @param accessKey
     */
    private void checkLogsForFirstMorningGreeting(final AccessKey accessKey) {
        final ArrayList<Logs> logs = new ArrayList<>();
        mDatabase.child("logs").orderByChild("location").startAt(getString(R.string.device_id)).endAt(getString(R.string.device_id)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Logs log = snapshot.getValue(Logs.class);
                    log.setKey(snapshot.getKey());

                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
                    String today = sdf.format(new Date());
                    String logDay = sdf.format(new Date(log.getTimestamp()));

                    if (today.equals(logDay)) {
                        logs.add(log);
                    }
                }

                int count = 0;
                for (Logs log : logs) {
                    if (accessKey.getKey().equals(log.getAccessKey())) {
                        count++;
                    }
                }

                if (count == 1) {
                    //first time person has entered today
                    getWeatherInfo(accessKey);
                } else {
                    playBoomMusic(accessKey.getKey());
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Turns off the LED after a set amount of time
     */
    private Runnable mOffRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the GPIO is already closed
            if (mRelayGpio == null) {
                return;
            }
            try {
                // Set the GPIO state to off
                Log.d("thing:MainActivity (run)", "Locking Door...");
                mRelayGpio.setValue(false);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove pending blink Runnable from the handler.
        mHandler.removeCallbacks(mOffRunnable);
        // Close the Gpio pin.
        Log.i(TAG, "Closing LED GPIO pin");
        try {
            mRelayGpio.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mRelayGpio = null;
        }
    }

    private class YahooWeatherInfoListener implements zh.wang.android.yweathergetter4a.YahooWeatherInfoListener {

        private AccessKey mAccessKey;

        public YahooWeatherInfoListener(AccessKey accessKey) {
            this.mAccessKey = accessKey;
        }

        @Override public void gotWeatherInfo(WeatherInfo weatherInfo, YahooWeather.ErrorType errorType) {
            Log.d("Sunset: ", weatherInfo.getAstronomySunset());
            Log.d("Humidity: ", weatherInfo.getAtmosphereHumidity());
            Log.d("Temp: ", weatherInfo.getCurrentTemp() + "");
            Log.d("ForecastInfo1Text: ", weatherInfo.getForecastInfo1().getForecastText());
            Log.d("ForecastInfo1HighLow: ", "High: " + weatherInfo.getForecastInfo1().getForecastTempHigh() + " Low: " + weatherInfo.getForecastInfo1().getForecastTempLow());
            Log.d("CurrentText: ", weatherInfo.getCurrentText());

            String greetingText = "Good ";

            SimpleDateFormat sdf = new SimpleDateFormat("a");
            if (sdf.format(new Date()).equalsIgnoreCase("am")) {
                greetingText += "morning";
            } else {
                greetingText += "evening";
            }

            if (null != mAccessKey.getName()) {
                greetingText += ", " + mAccessKey.getName() + ".";
            }

            String morningGreeting = greetingText + "The current temperature is " + weatherInfo.getCurrentTemp()
                    + " with a high of "
                    + weatherInfo.getForecastInfo1().getForecastTempHigh()
                    + " and a low of "
                    + weatherInfo.getForecastInfo1().getForecastTempLow()
                    + ". It is currently "
                    + weatherInfo.getCurrentText()
                    + " and later "
                    + weatherInfo.getForecastInfo1().getForecastText();

            tts.speak(morningGreeting, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
}

