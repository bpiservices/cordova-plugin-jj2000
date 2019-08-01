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
import java.io.InputStream;
import com.gemalto.jp2.JP2Decoder;

public class JJ2000 extends CordovaPlugin {
    private final static String TAG = JJ2000.class.getSimpleName();
    CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        Log.i(TAG, "execute: " + action);
        
        this.callbackContext = callbackContext;

        if ("convertJJ2000".equals(action)) {
            String data = "";
            try {
                data = args.getString(0);
            } catch (Exception ex) {
            }
            byte[] converted = convertPhoto(Base64.decode(data, Base64.NO_WRAP));
            if (converted == null) return true;
            
            String result = Base64.encodeToString(converted, Base64.NO_WRAP);
            callbackContext.success(result);
            return true;
        }

        callbackContext.error("Unknown command: " + action);
        return true;
    }

    private byte[] convertPhoto(byte[] photoBytes)
    {
        try {
            String photoMimeType = "";
            int startIndex = indexOf(photoBytes, new byte[] {(byte)0xFF, (byte)0xD8, (byte)0xFF});
            
            if (startIndex > 0) {
                Log.d(TAG, "startindex JPEG: " + startIndex);
                photoMimeType = "image/jpeg";
            } else {
                startIndex = indexOf(photoBytes, new byte[] {0x00, 0x00, 0x00, 0x00, 0x0C, 0x6A, 0x50, 0x20, 0x20, 0x0D, 0x0A, (byte)0x87, 0x0A}) + 1;
                Log.d(TAG, "startindex JPEG2000: " + startIndex);
                photoMimeType = "image/jp2";
            }
            
            photoBytes = subBytes(photoBytes, startIndex, photoBytes.length);
            
            InputStream inputStream = new ByteArrayInputStream(photoBytes);
            Bitmap androidBitmap;

            if (photoMimeType.equals("image/jpeg")) {
                androidBitmap = BitmapFactory.decodeStream(inputStream);
            }
            else {
                androidBitmap = new JP2Decoder(inputStream).decode();
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            androidBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "convertPhoto Error: " + e.getMessage());
            callbackContext.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
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
    
    private static byte[] subBytes(byte[] source, int srcBegin, int srcEnd) {
        byte destination[] = new byte[srcEnd - srcBegin];        
        System.arraycopy(source, srcBegin, destination, 0, srcEnd - srcBegin);
        return destination;
    }
    
    private static int indexOf(byte[] array, byte[] target) {
        if (target.length == 0) {
            return 0;
        }

        outer:
        for (int i = 0; i < array.length - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}