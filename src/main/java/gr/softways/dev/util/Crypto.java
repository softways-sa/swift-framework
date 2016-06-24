/**
 * Crypto.java
 *
 * Created on 17 Σεπτέμβριος 2001, 9:55 πμ
 */

package gr.softways.dev.util;


import javax.crypto.*;
import javax.crypto.spec.*;

/**
 *
 * @author  Administrator
 * @version 
 */
public class Crypto {
  
  private Crypto() {
  }
  
  /* 
   * Encrypts text calling doEncrypt and returns a String representing the encrypted
   * bytes in hex digits
   */
  public static String encrypt(String clearText) {
    byte[] cipherTextInBytes = doEncrypt(clearText);
    
    return bytesToHexString(cipherTextInBytes);
  }
  
  /* 
   * Decrypts cipher text(in hex digits) calling doDecrypt after transforming the
   * text to the corresponding byte array
   */  
  public static String decrypt(String cryptoText) throws Exception {
    return new String(doDecrypt(hexStringToBytes(cryptoText)));
  }
  
  private static byte[] doEncrypt(String clearText) {
    PBEKeySpec pbeKeySpec = null;
    PBEParameterSpec pbeParamSpec = null;
    SecretKeyFactory keyFac = null;
    
    byte[] ciphertext = "ciphertext".getBytes();
    
    try {
      // Create PBE parameter set
      java.security.Security.addProvider(new com.sun.crypto.provider.SunJCE());
      pbeParamSpec = new PBEParameterSpec(_salt, _count);
      
      // Convert private key into a SecretKey object, using a PBE key
      // factory.
      pbeKeySpec = new PBEKeySpec(_passKey.toCharArray());
      keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES", "SunJCE");
      SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
      
      // Create PBE Cipher
      Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES", "SunJCE");
      
      // Initialize PBE Cipher with key and parameters
      pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
            
      // Encrypt the cleartext
      ciphertext = pbeCipher.doFinal(clearText.getBytes());
    }
    catch(Exception e) {
      e.printStackTrace();
      ciphertext = null;
    }
    
    return ciphertext;
  }
  
  private static byte[] doDecrypt(byte[] eb) {
    PBEKeySpec pbeKeySpec = null;
    PBEParameterSpec pbeParamSpec = null;
    SecretKeyFactory keyFac = null;
    
    byte[] cleartext = "cleartext".getBytes();
    
    try {
      // Create PBE parameter set
      pbeParamSpec = new PBEParameterSpec(_salt, _count);
      
      // Convert private key into a SecretKey object, using a PBE key
      // factory.
      pbeKeySpec = new PBEKeySpec(_passKey.toCharArray());
      keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES", "SunJCE");
      SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
      
      // Create PBE Cipher
      Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES", "SunJCE");
      
      // Initialize PBE Cipher with key and parameters
      pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
      
      // Decrypt the ciphertext
      cleartext = pbeCipher.doFinal(eb);
    }
    catch(Exception e) {
      e.printStackTrace();
      cleartext = null;
    }
    
    return cleartext;
  }
    
  /**
   * Converts a byte to hex digit and writes to the supplied buffer
   */
  private static void byte2hex(byte b, StringBuffer buf) {
    char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                        '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    int high = ((b & 0xf0) >> 4);
    int low = (b & 0x0f);
    buf.append(hexChars[high]);
    buf.append(hexChars[low]);
  }

  /**
   * Converts a byte array to hex string
   */
  private static String bytesToHexString(byte[] block) {
    StringBuffer buf = new StringBuffer();
    int len = block.length;

    for (int i = 0; i < len; i++) {
      byte2hex(block[i], buf);
    } 
    return buf.toString();
  }        
    
  /**
   * Converts a hex string to byte array
   */
  private static byte [] hexStringToBytes(String hs) 
          throws Exception {
    int len = hs.length();
    int j=0;
    byte [] ba = null;
    if (len == 0)
      return (byte []) null;
    try {
      ba = new byte[len / 2];
      for (int i = 0; i < len; i+=2, j++) {
        ba[j] = (byte)Integer.parseInt(hs.substring(i, i+2), 16);
      }
    }
    catch(Exception e) {
      e.printStackTrace();
      ba = null;
    }
    
    return ba;
  }
  
  private static String _passKey = "m@rk3tG@rd3n";
 
  private static byte[] _salt = {
                          (byte)0xaa, (byte)0xff, (byte)0x34, (byte)0x54,      
                          (byte)0xb7, (byte)0x82, (byte)0xba, (byte)0xab
                        };
                        
  private static int _count = 3294;
  
  // encrypt("admin") = 583C6945FD0C7534
}