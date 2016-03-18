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


import android.content.Context;
import android.util.Base64;

import com.jsonstore.util.JSONStoreLogger;
import com.jsonstore.util.JSONStoreUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


public class FipsWrapper {

     private static final String LIBSSL_FILE_NAME = "libssl.so.1.0.0";
     private static MicroVPNLib nativeLib;
     private static final String ERROR_LOG_PREFIX = "Error processing X509Certificate: ";
     private static JSONStoreLogger logger = JSONStoreUtil.getCoreLogger();
     private static  byte[] _encryptAES (byte[] key, byte[] iv,  String to_encrypt )  {

          byte[] encryptedText = new byte[256];
          byte[] plaintext;
          Cipher cipher = null;
          try {
               //Get instance of cipher for aes
               cipher = Cipher.getInstance("AES/CBC/NoPadding");

               //Create hash for key using sha1
               MessageDigest sha = MessageDigest.getInstance("SHA-1");
               key = sha.digest(key);
               String keyString = SecurityUtils.encodeBytesAsHexString(key);
               key = Arrays.copyOf(keyString.getBytes("UTF-8"), 16); // use only first 128 bit

               SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

               IvParameterSpec ivSpec = new IvParameterSpec(iv);

               cipher.init(Cipher.ENCRYPT_MODE,  secretKey, ivSpec);
               plaintext = to_encrypt.getBytes("UTF-8");
               encryptedText = cipher.doFinal(plaintext);


          } catch(NoSuchAlgorithmException e){
               e.printStackTrace();

          } catch(NoSuchPaddingException e){
               e.printStackTrace();

          } catch(InvalidKeyException e){
               e.printStackTrace();

          } catch(IllegalBlockSizeException e){

               e.printStackTrace();
               System.out.println(e.toString());

          } catch(BadPaddingException e){
               e.printStackTrace();
               System.out.println(e.toString());

          } finally {
               return encryptedText;
          }

     }



     private static byte[] _decryptAES (byte[] key, byte[] iv, byte[] encryptedData){
          byte[] plaintext = new byte[256];
          Cipher cipher = null;
          try {
               //Get instance of cipher for aes
               cipher = Cipher.getInstance("AES/CBC/NoPadding");

               //Create hash for key using sha1
               MessageDigest sha = MessageDigest.getInstance("SHA-1");
               key = sha.digest(key);
               String keyString = SecurityUtils.encodeBytesAsHexString(key);
               key = Arrays.copyOf(keyString.getBytes("UTF-8"), 16); // use only first 128 bit


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
          byte[] decryptedBytes = _decryptAES(keyByteArray, ivByteArray, encryptedBytes);
          String decryptedText;
          try {
               CharsetDecoder charsetDecoder = Charset.forName("UTF-8") //$NON-NLS-1$
                    .newDecoder ();
               CharBuffer charBuffer = charsetDecoder.decode (ByteBuffer.wrap(decryptedBytes));
               decryptedText = new String(decryptedBytes, "UTF-8"); // in case the default charset is not UTF-8 //$NON-NLS-1$
          }
          catch (Throwable t) {
               t.printStackTrace();
               decryptedText = null;
          }
          Arrays.fill(decryptedBytes, (byte) 0);
          return decryptedText;
     }

     private static void saveCrtBundle(Context ctx) {
          TrustManagerFactory tmf = null;
          File localStorage = new File(JSONStoreUtil.getNoBackupFilesDir(ctx), "ca-bundle.crt");
          Exception ex = null;
          try {
               OutputStream ostr = new FileOutputStream(localStorage);

               tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
               tmf.init((KeyStore) null);

               X509TrustManager xtm = (X509TrustManager) tmf.getTrustManagers()[0];
               for (X509Certificate cert : xtm.getAcceptedIssuers()) {
                    try {
                         String certEnc = "-----BEGIN CERTIFICATE-----\n";
                         byte[] array = certEnc.getBytes("UTF-8");
                         ostr.write(array, 0, array.length);

                         array = cert.getEncoded();
                         array = Base64.encode(array, Base64.DEFAULT);
                         ostr.write(array, 0, array.length);

                         certEnc = "-----END CERTIFICATE-----\n";
                         array = certEnc.getBytes("UTF-8");
                         ostr.write(array, 0, array.length);
                    } catch (IOException e) {
                         logger.logError(ERROR_LOG_PREFIX, e);
                         // do not rethrow
                    } catch (CertificateEncodingException e) {
                         logger.logError(ERROR_LOG_PREFIX, e);
                         // do not rethrow
                    }
               }

               ostr.flush();
               ostr.close();
          } catch (IOException e) {
               ex = e;
          } catch (NoSuchAlgorithmException e) {
               ex = e;
          } catch (KeyStoreException e) {
               ex = e;
          }

          if (ex != null) {
               logger.logError(ERROR_LOG_PREFIX, ex);
          }
     }

     public static void enableFips(Context context){
          JSONStoreUtil.loadLib(context, LIBSSL_FILE_NAME);

          nativeLib = new MicroVPNLib();

          if(System.getProperty("javax.net.ssl.trustStore") != null) {
               File localStorage = new File(JSONStoreUtil.getNoBackupFilesDir(context), "ca-bundle.crt");
               if(!localStorage.exists()) {
                    saveCrtBundle(context);
               }
          } else {
               saveCrtBundle(context);
          }

          nativeLib.FIPSInit();
     }

}