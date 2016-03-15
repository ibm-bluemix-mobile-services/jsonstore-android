/*
 *     Copyright 2016 IBM Corp.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.jsonstore.security;

import android.os.Build;

import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SecurityUtils {

	private static final int BYTES_TO_BITS = 8;
	private static final int KEY_SIZE_AES256 = 32;
	public static final int PBKDF2_ITERATIONS = 10000;
     
     private SecurityUtils () {
     }

     public static byte[] decode (String key, String value, String iv)
          throws Exception {

          String decryptedString = FipsWrapper.decryptAES(key, iv, FipsWrapper.hexStringToByteArray (value));
           if (decryptedString == null || decryptedString.length () == 0) {
               throw new javax.crypto.BadPaddingException ("Decryption failed");
          } 
         return decryptedString.getBytes();
     }
     
     public static String encodeBytesAsHexString (byte bytes[]) {
          StringBuilder result = new StringBuilder();
          
          if (bytes != null) {
               for (byte curByte : bytes) {
                    result.append (String.format("%02X", curByte)); //$NON-NLS-1$
               }
          }
          
          return result.toString();
     }
     
     public static String encodeKeyAsHexString (SecretKey key) {
          return SecurityUtils.encodeBytesAsHexString(key.getEncoded());
     }
     
     public static byte[] encrypt (String key, String value, String iv)
          throws Exception {
          byte[] encryptedBytes = FipsWrapper.encryptAES(key, iv, value);
          return encryptedBytes;
     }
     
     public static byte[] generateIV (int numBytes) {
          byte[] iv = new byte[numBytes];
          new SecureRandom().nextBytes (iv);
          return iv;
     }

     public static SecretKey generateKey (String password, String salt, int pbkdf2Iterations) throws Exception {
    	 return generateKey(password, salt, pbkdf2Iterations, SecurityUtils.KEY_SIZE_AES256);
     }
     
     public static SecretKey generateKey (String password, String salt, int iterations, int keyLength) throws Exception {
    	 SecretKeyFactory pbkdf2Factory;
    	 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    	    // Use compatibility key factory required for backwards compatibility in API 19 and up.
    	    pbkdf2Factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1And8bit"); //$NON-NLS-1$
    	 } else {
    	    // Traditional key factory.
    	    pbkdf2Factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); //$NON-NLS-1$
    	 }
    	 
    	 PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes("UTF-8"), iterations, keyLength * BYTES_TO_BITS); //$NON-NLS-1$
    	 return pbkdf2Factory.generateSecret(keySpec);
     }
     
     //Called if the user requests to generate the key locally, we gen all 256 bits,
     //so no need to use PBKDF2 like the method above
     public static byte[] generateLocalKey(int numBytes){
          byte[] randBytes = new byte[numBytes];
          new SecureRandom().nextBytes (randBytes);
          return randBytes;
     }

     public static String getRandomString(int byteLength) {
          byte[] randomByteArray = generateLocalKey(byteLength);
          return encodeBytesAsHexString(randomByteArray);
     }
}

