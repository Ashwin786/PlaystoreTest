package com.oasys.playstore;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    private EditText editText,textView;
    private Button button, decrypt_button;
    String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        decrypt_button = (Button) findViewById(R.id.decrypt_button);
        textView = (EditText) findViewById(R.id.textView);
//        String text = "rcid=299900000002|dcode=999|userid=OASIS|password=Oasis23Nic1|dt=11/22/201910:56:48AM";
        text = "rcid=299900000002|dcode=999|userid=OASIS|password=Oasis23Nic1|dt=11/22/201910:56:48AM";
        editText.setText(text);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = editText.getText().toString();
                text += "|checksum=" + getCheckSum(text);
                String encrypt_data = null;
                try {
                    encrypt_data = encrypt(text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e(TAG, "encrypt: " + encrypt_data);
                textView.setText(encrypt_data);
            }
        });

        decrypt_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().length() == 0) {
                    Toast.makeText(MainActivity.this, "Enter value", Toast.LENGTH_SHORT).show();
                    return;
                }
                String decrypt_text = "Empty";
                try {
                    decrypt_text = decrypt(editText.getText().toString());
                } catch (Exception e) {
                    decrypt_text = e.toString();
                    e.printStackTrace();
                }
                textView.setText("" + decrypt_text);
            }
        });
//            Log.e(TAG, "decrypt: " + decrypt(decrypt_text));


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
