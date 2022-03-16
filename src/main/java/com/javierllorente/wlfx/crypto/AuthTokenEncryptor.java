/*
 * Copyright (C) 2022 Javier Llorente <javier@opensuse.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.javierllorente.wlfx.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author javier
 */
public class AuthTokenEncryptor implements StringEncryptor {

    private final Key secretKey;
    private final AlgorithmParameterSpec params;

    public AuthTokenEncryptor() {
            String encodedKey = "aS20LHInLKgvBT6LaqYxYw==";
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            secretKey = new SecretKeySpec(decodedKey, "AES");
            
            String encodedIv = "vNmLqJ2KTFJ8jzop/QOZtw==";
            byte[] decodedIv = Base64.getDecoder().decode(encodedIv);
            params = new IvParameterSpec(decodedIv);
    }

    private Cipher getCipher(int opmode) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(opmode, secretKey, params);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException 
                | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(AuthTokenEncryptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cipher;
    }
    
    @Override
    public String encrypt(String token) {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);        
        String encryptedToken = "";
        try {
            byte[] encryptedData = cipher.doFinal(token.getBytes());
            encryptedToken = Base64.getEncoder().encodeToString(encryptedData);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(AuthTokenEncryptor.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return encryptedToken;
    }


    @Override
    public String decrypt(String encryptedToken) {
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
        String decryptedToken = "";
        try {
            byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedToken));
            decryptedToken = new String(decryptedData, StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(AuthTokenEncryptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return decryptedToken;
    }

}
