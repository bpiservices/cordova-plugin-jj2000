package com.gridler.imatch;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class JJ2000 extends CordovaPlugin {
    private final static String TAG = JJ2000.class.getSimpleName();

    @Override
    public boolean execute(String action, JSONArray args,
                           final CallbackContext callbackContext) {
        Log.i(TAG, "execute: " + action);

        if ("convertJJ2000".equals(action)) {
            String data = "";
            try {
                data = args.getString(0);
            } catch (Exception ex) {
            }
            byte[] converted = convertPhoto(Base64.decode(data, Base64.NO_WRAP));

            String result = Base64.encodeToString(converted, Base64.NO_WRAP);
            callbackContext.success(result);
            return true;
        }

        callbackContext.error("Unknown command: " + action);
        return true;
    }

    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length*2];
        int v;

        for(int j=0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v>>>4];
            hexChars[j*2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    public static byte[] hexToByteArray(String hex) {
        hex = hex.length()%2 != 0?"0"+hex:hex;

        byte[] b = new byte[hex.length() / 2];

        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    private byte[] convertPhoto(byte[] photoBytes)
    {
        try {
            String startJPEG = "FFD8FF";
            String startJPEG2000 = "0000000C6A5020200D0A870A";
            int startIndex;

            String hex = byteArrayToHexString(photoBytes);
            if (hex.contains("FFD8FF")) {
                startIndex = (hex.indexOf(startJPEG));
                Log.d(TAG, "startindex JPEG: " + startIndex);
            } else {
                startIndex = (hex.indexOf(startJPEG2000));
                Log.d(TAG, "startindex JPEG2000: " + startIndex);
            }

            hex = hex.substring(startIndex);
            photoBytes = hexToByteArray(hex);

            InputStream inputStream = new ByteArrayInputStream(photoBytes);
            Bitmap androidBitmap;

            if (hex.contains("FFD8FF")) {
                androidBitmap = BitmapFactory.decodeStream(inputStream);
            }
            else {
                org.jmrtd.jj2000.Bitmap jj20000bitmap = org.jmrtd.jj2000.JJ2000Decoder.decode(inputStream);
                int[] intData = jj20000bitmap.getPixels();
                androidBitmap = Bitmap.createBitmap(intData, 0, jj20000bitmap.getWidth(), jj20000bitmap.getWidth(), jj20000bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            androidBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();

        } catch (IOException e) {
            Log.e(TAG, "convertPhoto Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}