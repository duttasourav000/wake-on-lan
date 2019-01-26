package dutta.software.wakeonlan;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

public class MyTriggerService extends IntentService {

    public static final String PARAM_IN_MSG = "imsg";
    public static final String HANDLER_IN_MSG = "hmsg";
    public static final String PARAM_OUT_MSG = "omsg";

    public MyTriggerService() {
        super("MyTriggerService");
    }

    // the method will be triggered in a loop
    @Override
    protected void onHandleIntent(Intent intent) {
        long timeout = 10 * 60 * 1000;

        // Ping to know if machine needs to be turned on
        JSONObject status = getHTTPResponse(BuildConfig.PROP_STATUS_URL);
        try {
            if (status != null) {
                timeout = status.getLong("ping-freq") * 60 * 1000;
            }

            if (status != null && status.getBoolean("status")) {
                for (int i = 0; i < 2; i++) {
                    wakeItUp(BuildConfig.PROP_IP, BuildConfig.PROP_MAC, BuildConfig.PROP_PORT);
                }

                sendToast(intent, "Wake On Lan!");
                JSONObject response = getHTTPResponse(BuildConfig.PROP_STATUS_OFF_URL);
            }
            else {
                sendToast(intent, "Ignored.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("MyTriggerService", "Triggered");

        boolean shouldRecur = intent.getBooleanExtra(PARAM_IN_MSG, false);
        if (shouldRecur) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            // make the pending intent
            PendingIntent pendingIntent = PendingIntent
                    .getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // schedule the intent for future
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + timeout, pendingIntent);
        }
    }

    // gets response in json formal from the given url
    private JSONObject getHTTPResponse(String url) {
        JSONObject responseJSON = null;

        try {
            HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();
            System.out.println(url);
            con.setRequestMethod("GET");
            con.connect();

            int responseCode = con.getResponseCode();
            Log.d("MyTriggerService", url + String.valueOf(responseCode));

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            con.disconnect();

            responseJSON = new JSONObject(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseJSON;
    }

    private void wakeItUp(String ipStr, String macStr, int port) {

        try {
            byte[] macBytes = getMacBytes(macStr);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(ipStr);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();

            System.out.println("Wake-on-LAN packet sent.");
        }
        catch (Exception e) {
            System.out.println("Failed to send Wake-on-LAN packet: + e");
            System.exit(1);
        }
    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }

    private static void sendToast(Intent intent, String text) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Messenger messenger = (Messenger) bundle.get(HANDLER_IN_MSG);
            Message msg = Message.obtain();

            Bundle outBundle = new Bundle();
            outBundle.putString(PARAM_OUT_MSG, text);
            msg.setData(outBundle); //put the data here
            try {
                messenger.send(msg);
            } catch (RemoteException e) {
                Log.i("error", "error");
            }
        }
    }
}