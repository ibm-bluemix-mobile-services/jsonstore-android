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


import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class FipsWrapper {


     private static  byte[] _encryptAES (byte[] key, byte[] iv,  String to_encrypt )  {

          byte[] encryptedText = new byte[256];
          byte[] plaintext;
          Cipher cipher = null;
          try {
               //Get instance of cipher for aes
               cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

               //Create hash for key using sha1
               MessageDigest sha = MessageDigest.getInstance("SHA-1");
               key = sha.digest(key);
               key = Arrays.copyOf(key, 16); // use only first 128 bit

               SecureRandom secureRandom = new SecureRandom();

               secureRandom.nextBytes(iv);

               SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

               IvParameterSpec ivSpec = new IvParameterSpec(iv);

               cipher.init(Cipher.ENCRYPT_MODE,  secretKey, ivSpec);
               plaintext = to_encrypt.getBytes();
               encryptedText = cipher.doFinal(plaintext);


          } catch(NoSuchAlgorithmException e){
               e.printStackTrace();

          } catch(NoSuchPaddingException e){
               e.printStackTrace();

          } catch(InvalidKeyException e){
               e.printStackTrace();

          } catch(IllegalBlockSizeException e){
               e.printStackTrace();

          } catch(BadPaddingException e){
               e.printStackTrace();

          } finally {
               return encryptedText;
          }

     }

     private static byte[] _decryptAES (byte[] key, byte[] iv, byte[] encryptedData){
          byte[] plaintext = new byte[256];
          Cipher cipher = null;
          try {
               //Get instance of cipher for aes
               cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

               //Create hash for key using sha1
               MessageDigest sha = MessageDigest.getInstance("SHA-1");
               key = sha.digest(key);
               key = Arrays.copyOf(key, 16); // use only first 128 bit

               SecureRandom secureRandom = new SecureRandom();

               secureRandom.nextBytes(iv);

               SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

               IvParameterSpec ivSpec = new IvParameterSpec(iv);

               cipher.init(Cipher.DECRYPT_MODE,  secretKey, ivSpec);
               plaintext = cipher.doFinal(encryptedData);


          } catch(NoSuchAlgorithmException e){
               e.printStackTrace();

          } catch(NoSuchPaddingException e){
               e.printStackTrace();

          } catch(InvalidKeyException e){
               e.printStackTrace();

          } catch(IllegalBlockSizeException e){
               e.printStackTrace();

          } catch(BadPaddingException e){
               e.printStackTrace();

          } finally {
               return plaintext;
          }

     }


     public static final byte[] hexStringToByteArray(String s) {
          int len = s.length();
          byte[] data = new byte[len / 2];

          for(int i = 0; i < len; i += 2) {
               data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
          }

          return data;
     }

     public static byte[] encryptAES (String key, String iv, String clearText) {
          byte[] keyByteArray = hexStringToByteArray(key);
          byte[] ivByteArray = hexStringToByteArray(iv);
          byte[] encBytes = _encryptAES(keyByteArray ,ivByteArray,  clearText);
          return encBytes;
     }

     // Throws exception if can't decode
     public static String decryptAES (String key, String iv,
          byte[] encryptedBytes) {
          byte[] keyByteArray = hexStringToByteArray(key);
          byte[] ivByteArray = hexStringToByteArray(iv);
          byte[] decryptedBytes = _decryptAES (keyByteArray, ivByteArray, encryptedBytes);
          String decryptedText;
          try {
               CharsetDecoder charsetDecoder = Charset.forName("UTF-8") //$NON-NLS-1$
                    .newDecoder ();
               CharBuffer charBuffer = charsetDecoder.decode (ByteBuffer.wrap(decryptedBytes));
               decryptedText = new String(decryptedBytes, "UTF-8"); // in case the default charset is not UTF-8 //$NON-NLS-1$
          }
          catch (Throwable t) {
               decryptedText = null;
          }
          Arrays.fill(decryptedBytes, (byte) 0);
          return decryptedText;
     }

}