package com.proyecto.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class MessageCryptoService {

    private static final String PREFIX = "ENC1:";
    private static final int NONCE_LEN = 12;     // recomendado para GCM
    private static final int TAG_BITS = 128;     // 16 bytes
    private final SecureRandom random = new SecureRandom();
    private final SecretKeySpec key;

    public MessageCryptoService(@Value("${app.msg.secret:CLAVE-DEMO-CAMBIALA}") String secret) {
        this.key = new SecretKeySpec(sha256(secret), "AES");
    }

    public String encrypt(String plain) {
        if (plain == null) return null;
        if (plain.startsWith(PREFIX)) return plain; // ya cifrado

        try {
            byte[] nonce = new byte[NONCE_LEN];
            random.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));

            byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            return PREFIX
                    + Base64.getEncoder().encodeToString(nonce)
                    + ":"
                    + Base64.getEncoder().encodeToString(ct);
        } catch (Exception e) {
            throw new RuntimeException("Error cifrando mensaje", e);
        }
    }

    public String decrypt(String stored) {
        if (stored == null) return null;
        if (!stored.startsWith(PREFIX)) return stored; // compatibilidad con mensajes viejos sin cifrar

        try {
            String payload = stored.substring(PREFIX.length());
            String[] parts = payload.split(":", 2);
            if (parts.length != 2) return stored;

            byte[] nonce = Base64.getDecoder().decode(parts[0]);
            byte[] ct = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));

            byte[] pt = cipher.doFinal(ct);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Si algo salió mal, devolvemos lo que hay (no reventar la UI)
            return stored;
        }
    }

    private byte[] sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(s.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
