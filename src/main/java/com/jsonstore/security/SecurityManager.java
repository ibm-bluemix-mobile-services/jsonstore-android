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

public class SecurityManager {

     private static final int IV_NUM_BYTES = 16;
     private static final int LOCAL_KEY_NUM_BYTES = 32;

     private static SecurityManager instance;

     private Keychain keychain;

     private SecurityManager (Context context) {
          this.keychain = new Keychain (context);
     }

     public static synchronized SecurityManager getInstance (Context context) {
          if (SecurityManager.instance == null) {
               SecurityManager.instance = new SecurityManager (context);
          }

          return SecurityManager.instance;
     }

     public String getDPK (String password, String username) throws Exception {
          String decodedDPK;
          DPKBean dpkBean = this.keychain.getDPKBean (username);
          String pwKey = SecurityUtils.encodeKeyAsHexString (SecurityUtils
               .generateKey (password, dpkBean.getSalt (), dpkBean.getIterations()));

          // The DPK is base-64 encoded, so decode it before decrypting.

          decodedDPK = new String(Base64.decode(dpkBean.getEncryptedDPK(),
                  Base64.DEFAULT));

          return new String(SecurityUtils.decode (pwKey, decodedDPK,
               dpkBean.getIV ()));
     }

     public String getSalt (String username) throws Exception {
          DPKBean dpkBean = this.keychain.getDPKBean (username);

          if (dpkBean == null) {
               return null;
          }

          return dpkBean.getSalt ();
     }

     public boolean isDPKAvailable (String username) {
          return this.keychain.isDPKAvailable (username);
     }

     public void destroyKeychain () {
          this.keychain.destroy ();
     }

     public boolean storeDPK (String password, String username,
          String clearDPK, String salt, boolean isUpdate, int pbkdf2Iterations)
          throws Exception {
          String dpk = clearDPK;

          DPKBean dpkBean;
          String encryptedDPK;
          String iv;
          String pwKey;

          if (clearDPK == null || clearDPK.equals("")) {
               dpk = SecurityUtils.encodeBytesAsHexString (SecurityUtils
                    .generateLocalKey (SecurityManager.LOCAL_KEY_NUM_BYTES));
          }else if (!isUpdate) {
               // If we're doing an update, the clearDPK should already be the
               // derived one.

               dpk = SecurityUtils.encodeKeyAsHexString (SecurityUtils
                    .generateKey (clearDPK, salt, pbkdf2Iterations));
          }

          iv = SecurityUtils.encodeBytesAsHexString (SecurityUtils
               .generateIV (SecurityManager.IV_NUM_BYTES));
          pwKey = SecurityUtils.encodeKeyAsHexString (SecurityUtils
               .generateKey (password, salt, pbkdf2Iterations));

          // Encrypt the DPK and store everything in a bean that can be stored
          // in the keychain.

          encryptedDPK = Base64.encodeToString (SecurityUtils
                  .encodeBytesAsHexString(SecurityUtils.encrypt(pwKey, dpk, iv))
                  .getBytes(), Base64.DEFAULT);

          dpkBean = new DPKBean (encryptedDPK, iv, salt, pbkdf2Iterations);

          // Finally, save everything in the keychain.

          this.keychain.setDPKBean (username, dpkBean);

          return false;
     }


}
