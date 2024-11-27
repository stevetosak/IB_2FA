package mk.finki.ukim.ib.security_ib.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PsEncode {


    private final int iterations;

    public PsEncode(int strength) {

        strength = Math.min(strength, 20);
        this.iterations = (int) Math.pow(strength,5);
        System.out.println("ITERATIONS: " + iterations);
    }

    public String encode(String password, int saltLength) throws NoSuchAlgorithmException {
        SecureRandom sr = new SecureRandom();
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] salt = new byte[saltLength];
        sr.nextBytes(salt);

        byte[] combined = combine(passwordBytes, salt);
        combined = hash(combined,iterations);

        return String.format(
                "%s$%s",
                Base64.getEncoder().encodeToString(combined),
                Base64.getEncoder().encodeToString(salt));
    }

    private byte[] combine(byte[] pass,byte[] salt) {
        byte[] combined = new byte[pass.length + salt.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(pass, 0, combined, salt.length, pass.length);

        return combined;

    }

    private static byte[] hash(byte[] bytes, int iterations) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            bytes = md.digest(bytes);
        }
        long end = System.nanoTime();

        System.out.println("Hash time: " + ((end - start) / (Math.pow(10, 6))) + " ms");

        return bytes;
    }

    public boolean matches(String inputPassword, String dbPassword) throws NoSuchAlgorithmException {

        String[] passwordSplit = dbPassword.split("\\$");
        String hashedPassword = passwordSplit[0];
        String salt = passwordSplit[passwordSplit.length - 1];

        byte[] inputPasswordBytes = inputPassword.getBytes(StandardCharsets.UTF_8);
        byte[] saltBytes =  Base64.getDecoder().decode(salt);


        byte[] combined = combine(inputPasswordBytes, saltBytes);
        byte[] hashedInputBytes = hash(combined,iterations);

        String encoded = Base64.getEncoder().encodeToString(hashedInputBytes);

        return encoded.equals(hashedPassword);

    }

}
