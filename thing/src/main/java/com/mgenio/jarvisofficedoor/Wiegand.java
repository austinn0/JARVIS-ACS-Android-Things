package com.mgenio.jarvisofficedoor;

import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mgenio.jarvisofficedoor.interfaces.WiegandInterface;

import java.io.IOException;

/**
 * @author bilal
 */
public class Wiegand {
    public static final String TAG = "thing:Wiegand";

    long lastWiegand = 0;
    long sysTick = 0;
    int bitCount = 0;

    private String PIN = "";
    private String stream = "";
    final private Handler handler = new Handler();

    private static final String GPIO_PIN_D0_Name = "BCM4";
    private static final String GPIO_PIN_D1_Name = "BCM5";

    private Gpio mWiegand34GpioD0;
    private Gpio mWiegand34GpioD1;

    private DatabaseReference mDatabase;
    private WiegandInterface callback;

    public void setLoginCallback(WiegandInterface callback) {
        this.callback = callback;
    }

    public void begin() {

        Log.d(TAG + "(begin)", "begin");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("mgenio");

        lastWiegand = 0;
        bitCount = 0;
        sysTick = System.currentTimeMillis();

        PeripheralManagerService service = new PeripheralManagerService();
        Log.d("thing:Wiegand (begin)", "Available GPIO: " + service.getGpioList());

        try {
            // Step 1. Create GPIO connection.
            mWiegand34GpioD0 = service.openGpio(GPIO_PIN_D0_Name);
            mWiegand34GpioD1 = service.openGpio(GPIO_PIN_D1_Name);
            // Step 2. Configure as an input.
            mWiegand34GpioD0.setDirection(Gpio.DIRECTION_IN);
            mWiegand34GpioD1.setDirection(Gpio.DIRECTION_IN);
            // Step 3. Enable edge trigger events.
            mWiegand34GpioD0.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mWiegand34GpioD1.setEdgeTriggerType(Gpio.EDGE_FALLING);

            //Testing what this do
            //mWiegand34GpioD0.setActiveType(Gpio.ACTIVE_HIGH);

            // Step 4. Register an event callback.
            mWiegand34GpioD0.registerGpioCallback(new GpioEdgeCallback("0"));
            mWiegand34GpioD1.registerGpioCallback(new GpioEdgeCallback("1"));
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    private Runnable kickTimer = new Runnable() {
        @Override public void run() {
            if (stream.length() == 8) {
                Log.d(TAG, "8 Bits: " + stream);
                Log.d(TAG, "Low Nibble: " + stream.substring(0, 4));
                Log.d(TAG, "High Nibble: " + stream.substring(4, 8));

                if (IS_NOT(stream.substring(0, 4), stream.substring(4, 8))) {
                    doKeypadDecode();
                    sysTick = System.currentTimeMillis();
                } else {
                    Log.w(TAG + "(onGpioEdge)", "Invalid parity");
                }
            } else if (stream.length() == 26) {
                Log.d(TAG, "26 Bits: " + stream);
                doCardDecode();
            } else {
                //Logs.w(TAG + "(onGpioEdge)", "Read " + stream.length() + " bits");
            }

            //reset everything
            stream = "";
            bitCount = 0;
            lastWiegand = sysTick;
        }
    };

    /**
     * @param lowNibble
     * @param highNibble
     * @return
     */
    private boolean IS_NOT(String lowNibble, String highNibble) {
        boolean isNot = true;

        for (int i = 0; i < lowNibble.length(); i++) {
            if (lowNibble.charAt(i) == highNibble.charAt(i)) {
                isNot = false;
            }
        }

        return isNot;
    }

    /**
     * @param parityBit
     * @param stream
     * @return
     */
    private boolean evenParityCheck(String parityBit, String stream) {
        Log.i(TAG + "(evenParityCheck)", parityBit);
        Log.i(TAG + "(evenParityCheck)", stream);
        boolean isValid = false;

        int counter = 0;
        for (int i = 0; i < stream.length(); i++) {
            if (stream.charAt(i) == '1') {
                counter++;
            }
        }

        if ((counter % 2 == 0) && parityBit.equals("1")) {
            isValid = true;
        } else if ((counter % 2 != 0) && parityBit.equals("0")) {
            isValid = true;
        }

        return isValid;
    }

    /**
     * @param parityBit
     * @param stream
     * @return
     */
    private boolean oddParityCheck(String parityBit, String stream) {
        Log.i(TAG + "(oddParityCheck)", parityBit);
        Log.i(TAG + "(oddParityCheck)", stream);
        boolean isValid = false;

        int counter = 0;
        for (int i = 0; i < stream.length(); i++) {
            if (stream.charAt(i) == '1') {
                counter++;
            }
        }

        if ((counter % 2 == 0) && parityBit.equals("0")) {
            isValid = true;
        } else if ((counter % 2 != 0) && parityBit.equals("1")) {
            isValid = true;
        }

        return isValid;
    }

    /**
     *
     */
    private void doCardDecode() {
        String evenParityBit = String.valueOf(stream.charAt(0));
        String oddParityBit = String.valueOf(stream.charAt(stream.length() - 1));

        String facilityNumber = stream.substring(1, 9);
        String cardNumber = stream.substring(9, stream.length() - 1);

        Log.d(TAG + "(doCardDecode)", (evenParityCheck(evenParityBit, stream.substring(1, stream.length()))) + "");
        Log.d(TAG + "(doCardDecode)", (oddParityCheck(oddParityBit, stream.substring(1, stream.length()))) + "");

        int facilityDecimal = Integer.parseInt(facilityNumber, 2);
        int cardDecimal = Integer.parseInt(cardNumber, 2);

        String facilityHex = Integer.toString(facilityDecimal, 16);
        String cardHex = Integer.toString(cardDecimal, 16);

        Log.d(TAG + "(doCardDecode)", "Facility Number: " + facilityNumber + " - " + facilityDecimal + " - " + facilityHex);
        Log.d(TAG + "(doCardDecode)", "Card Number: " + cardNumber + " - " + cardDecimal + " - " + cardHex);

        callback.onCardWiegandRecieved(facilityNumber, cardNumber);
//        Data data = new Data();
//        Facility facility = new Facility();
//        Card card = new Card();
//
//        facility.setBinary(facilityNumber);
//        facility.setHex(facilityHex);
//        facility.setDecimal(facilityDecimal);
//
//        card.setBinary(cardNumber);
//        card.setHex(cardHex);
//        card.setDecimal(cardDecimal);
//
//        data.setFacility(facility);
//        data.setCard(card);
//
//        mDatabase.push().setValue(data);
    }

    /**
     *
     */
    private void doKeypadDecode() {
        String highNibble = stream.substring(4, 8);
        int key = Integer.parseInt(highNibble, 2);

        switch (key) {
            case 10:
                System.out.println("Key: *");
                break;
            case 11:
                System.out.println("Key: #");
                System.out.println("PIN: " + PIN);
                callback.onKeypadWiegandRecieved(PIN);
                PIN = "";
                break;
            default:
                PIN += key;
                System.out.println("Key: " + key);
                break;
        }
    }

    /**
     * GpioCallback with constructor function
     */
    private class GpioEdgeCallback extends GpioCallback {

        private String mDataPulse;

        public GpioEdgeCallback(String dataPulse) {
            this.mDataPulse = dataPulse;
        }

        @Override public boolean onGpioEdge(Gpio gpio) {
            Log.d(TAG, "onGpioEdge");
            bitCount++; // Increament bit count for Interrupt connected to D0
            stream += this.mDataPulse;

            lastWiegand = sysTick;

            //every time a new signal comes in, delay 200ms before processing
            handler.postDelayed(kickTimer, 200);

            return true;
        }

    }
}
