// utils/EncryptionUtils.java
package com.example.memorai.utils;

import android.util.Base64;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    public static String encrypt(String key, String input) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, generateKey(key));
        byte[] encryptedBytes = cipher.doFinal(input.getBytes());
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

    public static String decrypt(String key, String encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, generateKey(key));
        byte[] decoded = Base64.decode(encrypted, Base64.DEFAULT);
        byte[] decryptedBytes = cipher.doFinal(decoded);
        return new String(decryptedBytes);
    }

    private static Key generateKey(String key) throws Exception {
        byte[] keyBytes = key.getBytes("UTF-8");
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}
