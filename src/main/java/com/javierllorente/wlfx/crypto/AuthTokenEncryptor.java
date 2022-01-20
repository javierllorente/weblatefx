/*
 * Copyright (C) 2022 Javier Llorente <javier@opensuse.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
