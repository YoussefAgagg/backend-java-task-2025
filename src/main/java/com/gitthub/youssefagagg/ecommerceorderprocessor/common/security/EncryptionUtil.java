package com.gitthub.youssefagagg.ecommerceorderprocessor.common.security;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for encryption and decryption operations.
 */
@Component
public class EncryptionUtil {

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH = 128;

  @Value("${encryption.secret-key:defaultSecretKeyWhichShouldBeChanged}")
  private String secretKeyString;

  private SecretKey secretKey;

  /**
   * Generate a new secret key for AES encryption.
   *
   * @return Base64 encoded secret key
   */
  @SneakyThrows
  public static String generateSecretKey() {

    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    keyGenerator.init(256);
    SecretKey key = keyGenerator.generateKey();
    return Base64.getEncoder().encodeToString(key.getEncoded());
  }

  /**
   * Initialize the secret key.
   */
  public void init() {
    if (secretKey == null) {
      byte[] decodedKey = Base64.getDecoder().decode(secretKeyString);
      secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
  }

  /**
   * Encrypt the given text.
   *
   * @param plainText text to encrypt
   * @return encrypted text in Base64 format
   */
  @SneakyThrows()
  public String encrypt(String plainText) {

    init();

    // Generate a random IV
    byte[] iv = new byte[GCM_IV_LENGTH];
    new SecureRandom().nextBytes(iv);

    // Initialize cipher for encryption
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

    // Encrypt
    byte[] encryptedText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

    // Combine IV and encrypted text
    byte[] combined = new byte[iv.length + encryptedText.length];
    System.arraycopy(iv, 0, combined, 0, iv.length);
    System.arraycopy(encryptedText, 0, combined, iv.length, encryptedText.length);

    // Encode to Base64
    return Base64.getEncoder().encodeToString(combined);
  }

  /**
   * Decrypt the given encrypted text.
   *
   * @param encryptedText Base64 encoded encrypted text
   * @return decrypted text
   */
  @SneakyThrows
  public String decrypt(String encryptedText) {

    init();

    // Decode from Base64
    byte[] combined = Base64.getDecoder().decode(encryptedText);

    // Extract IV and encrypted text
    byte[] iv = new byte[GCM_IV_LENGTH];
    byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
    System.arraycopy(combined, 0, iv, 0, iv.length);
    System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

    // Initialize cipher for decryption
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

    // Decrypt
    byte[] decryptedText = cipher.doFinal(encrypted);

    return new String(decryptedText, StandardCharsets.UTF_8);

  }
}