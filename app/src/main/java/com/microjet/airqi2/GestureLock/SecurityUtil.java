package com.microjet.airqi2.GestureLock;

import android.annotation.SuppressLint;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtil {
    private static final String CIPHER_MODE = "AES/ECB/PKCS5Padding";
    private static final String MASTER_PASSWORD = "Test123454321";

    private static SecretKeySpec createKey(String password) {
        byte[] data = null;

        if (password == null) {
            password = "";
        }

        StringBuilder sb = new StringBuilder(32);

        sb.append(password);

        while (sb.length() < 32) {
            sb.append("0");
        }

        if (sb.length() > 32) {
            sb.setLength(32);
        }

        try {
            data = sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new SecretKeySpec(data, "AES");
    }

    public static String encrypt(final String content) {
        return encrypt(content, MASTER_PASSWORD);
    }

    @SuppressLint("GetInstance")
    private static byte[] encrypt(byte[] content, String password) {
        try {
            SecretKeySpec key = createKey(password);
            Cipher cipher = Cipher.getInstance(CIPHER_MODE);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String encrypt(String content, String password) {
        byte[] data = null;

        try {
            data = content.getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        data = encrypt(data, password);

        return byte2hex(data);
    }

    public static String decrypt(final String content) {
        return decrypt(content, MASTER_PASSWORD);
    }

    @SuppressLint("GetInstance")
    private static byte[] decrypt(byte[] content, String password) {
        try {
            SecretKeySpec key = createKey(password);
            Cipher cipher = Cipher.getInstance(CIPHER_MODE);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String decrypt(String content, String password) {
        byte[] data = null;

        try {
            data = hex2byte(content);
        } catch (Exception e) {
            e.printStackTrace();
        }

        data = decrypt(data, password);

        if (data == null) {
            return null;
        }

        String result = null;

        try {
            result = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static String byte2hex(byte[] b) { // 一個字節的數，
        StringBuilder sb = new StringBuilder(b.length * 2);
        String tmp;

        for (byte aB : b) {
            // 整數轉成十六進制表示
            tmp = (Integer.toHexString(aB & 0XFF));
            if (tmp.length() == 1) {
                sb.append("0");
            }
            sb.append(tmp);
        }
        return sb.toString().toUpperCase(); // 轉成大寫
    }

    private static byte[] hex2byte(String inputString) {
        if (inputString == null || inputString.length() < 2) {
            return new byte[0];
        }

        inputString = inputString.toLowerCase();

        int l = inputString.length() / 2;
        byte[] result = new byte[l];

        for (int i = 0; i < l; ++i) {
            String tmp = inputString.substring(2 * i, 2 * i + 2);
            result[i] = (byte) (Integer.parseInt(tmp, 16) & 0xFF);
        }

        return result;
    }
}

