package com.oasys.playstore;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;

import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String text = "rcid=299900000002|dcode=999|userid=OASIS|password=O@s$s@!@#4|dt=11/22/201910:56:48AM";
        String decrypt_text = "71ZyPBqKghnz50sJx4YWIUsfNyT27TD80vY72D1ySgM=";
        try {
            String id = "5120d687f85441c";
//            String encrypt_data = encrypt(id);
//            Log.e(TAG, "id: " + encrypt_data);
            text += "|checksum=" + getCheckSum(text);
            String encrypt_data = encrypt(text);
            Log.e(TAG, "encrypt: " + encrypt_data);
//            Log.e(TAG, "decrypt: " + decrypt(decrypt_text));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String getCheckSum(String str1) {
        return getMD511(str1);
    }

    public String getMD511(String md5Input) {
        final MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(md5Input.getBytes(Charset.forName("UTF8")));
            final byte[] resultByte = messageDigest.digest();
            final String result = new String(Hex.encodeHex(resultByte));
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String decrypt(String text) throws Exception {
        byte[] decrypt_text = Base64.decode(text, Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        byte[] keyBytes = new byte[16];
        byte[] pwdBytes = getKeyBytes();
        int len = pwdBytes.length;
        if (len > keyBytes.length)
            len = keyBytes.length;
        System.arraycopy(pwdBytes, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] results = cipher.doFinal(decrypt_text);
        return new String(results); // it returns the result as a String
    }

    public String encrypt(String text) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        byte[] keyBytes = new byte[16];
        byte[] pwdBytes = getKeyBytes();
        int len = pwdBytes.length;
        if (len > keyBytes.length)
            len = keyBytes.length;
        System.arraycopy(pwdBytes, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] results = cipher.doFinal(text.getBytes("UTF-8"));
        return Base64.encodeToString(results, Base64.DEFAULT); // it returns the result as a String
    }

    private byte[] getKeyBytes() {
        InputStream inputStream = null;
        AssetManager assetManager = getAssets();
        try {
            inputStream = assetManager.open("epos_key.key");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String keys = null;
        byte[] buffer = new byte[0];
        try {
            int length = inputStream.available();
            buffer = new byte[length];
            int count;
            int sum = 0;
            while ((count = inputStream.read(buffer, sum, length - sum)) > 0)
                sum += count;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buffer;
    }
}
