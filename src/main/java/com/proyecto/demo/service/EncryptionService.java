package com.proyecto.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final int KEY_SIZE_BITS = 256;
    private static final int ITERATIONS = 65536;
    private static final int SALT_LENGTH = 16;   // bytes
    private static final int NONCE_LENGTH = 12;  // bytes (96 bits para GCM)

    private final String masterKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptionService(@Value("${safebox.master-key:}") String masterKeyFromProps) {
        // prioridad a variable de entorno
        String envKey = System.getenv("SAFEBOX_MASTER_KEY");
        this.masterKey = (envKey != null && !envKey.isBlank()) ? envKey : masterKeyFromProps;

        if (this.masterKey == null || this.masterKey.isBlank()) {
            throw new IllegalStateException(
                    "Configurar SAFEBOX_MASTER_KEY en variables de entorno o safebox.master-key en application.properties");
        }
    }

    public static class EncryptionResult {
        private final String contentEnc;
        private final String nonce;
        private final String kdfSalt;

        public EncryptionResult(String contentEnc, String nonce, String kdfSalt) {
            this.contentEnc = contentEnc;
            this.nonce = nonce;
            this.kdfSalt = kdfSalt;
        }

        public String getContentEnc() { return contentEnc; }
        public String getNonce() { return nonce; }
        public String getKdfSalt() { return kdfSalt; }
    }

    public EncryptionResult encrypt(String plaintext) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);

            SecretKey key = deriveKey(masterKey.toCharArray(), salt);

            byte[] nonce = new byte[NONCE_LENGTH];
            secureRandom.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherBytes = cipher.doFinal(plaintext.getBytes("UTF-8"));

            return new EncryptionResult(
                    Base64.getEncoder().encodeToString(cipherBytes),
                    Base64.getEncoder().encodeToString(nonce),
                    Base64.getEncoder().encodeToString(salt)
            );
        } catch (Exception e) {
            throw new RuntimeException("Error cifrando contenido", e);
        }
    }

    public String decrypt(String contentEnc, String nonceB64, String saltB64) {
        try {
            byte[] cipherBytes = Base64.getDecoder().decode(contentEnc);
            byte[] nonce = Base64.getDecoder().decode(nonceB64);
            byte[] salt = Base64.getDecoder().decode(saltB64);

            SecretKey key = deriveKey(masterKey.toCharArray(), salt);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Error descifrando contenido", e);
        }
    }

    private SecretKey deriveKey(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_SIZE_BITS);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}
