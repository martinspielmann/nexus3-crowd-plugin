package com.pingunaut.nexus3.crowd.plugin.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;

public class PasswordHasher {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordHasher.class);

    private static final Random RANDOM = new SecureRandom();
    private static final int ITERATIONS = 1024;
    private static final int KEY_LENGTH = 256;

    private PasswordHasher() {
    }

    public static byte[] getNextSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return salt;
    }

    public static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Error while hashing password", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean isPasswordCorrect(char[] passwordToCheck, byte[] salt, byte[] expectedHash) {
        byte[] hashToCheck = hash(passwordToCheck, salt);
        Arrays.fill(passwordToCheck, Character.MIN_VALUE);
        if (hashToCheck.length != expectedHash.length) {
            return false;
        }
        for (int i = 0; i < hashToCheck.length; i++) {
            if (hashToCheck[i] != expectedHash[i]) {
                return false;
            }
        }
        return true;
    }
}
