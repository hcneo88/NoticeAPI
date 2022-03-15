package org.eservice.notice.component.security;

//https://stackoverflow.com/questions/50679221/aes-128-encryption-in-angular-4-and-decryption-in-java

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
//import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eservice.notice.constants.SecurityConstants;
import org.eservice.notice.repository.ConfigRepository;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Crypto {

    @Autowired
    ConfigRepository configRepo;
    @Autowired
    ApplicationContext context;
    /* @Autowired
    public CryptoKeys crytoKeys;
    */

    private KeyVault keyVault;

    static final int DEFAULTJWTDURATION = 920000;// 15++ minutes
    //static final String filePath = "vault.dat";

    public Crypto() {
        secretKey = KEYPREFIX + randString(KEYLEN - KEYPREFIX.length()); // Create default
        this.setSecretKey(secretKey); 
    }

    public Crypto(String secretKey) {
        this.setSecretKey(secretKey); // replace default if secretKey's length = KEYLEN
    }

    // For illustration purpoes on how to use seal objects
    // @PostConstruct
    /*public void demoLoadConfigFromKeyVault() {

        if (!Paths.get(filePath).toFile().exists()) {
            log.error("Unable to locate the key vault file (" + filePath + ").  Refer to initVault() to create it.");
            SpringApplication.exit(context);
        }

        this.keyVault = new KeyVault();
        String errMsg = "";

        boolean retCode = this.keyVault.loadVault(filePath);
        if (retCode != true) {
            log.error("Unable to load key vault with filename " + filePath);
            SpringApplication.exit(context);
        }

        String jwtSecret = (String) keyVault.getItem("JWTSecret");
        String jwtIssuer = (String) keyVault.getItem("JWTIssuer");
        String jwtVersion = (String) keyVault.getItem("JWTVersion");
        int jwtDuration = (int) keyVault.getItem("JWTDuration");
        if (jwtSecret == null || jwtSecret.length() != 64) {
            errMsg += "(a) JWTSecret";
        }
        ;
        if (jwtIssuer == null) {
            errMsg += "(b) JWTIssuer";
        }
        ;
        if (jwtVersion == null) {
            errMsg += "(c) JWTVersion";
        }
        ;
        if (jwtDuration == 0) {
            jwtDuration = DEFAULTJWTDURATION;
        }
        ;

        String serverSecret = (String) keyVault.getItem("ServerSecret");
        if (serverSecret == null) {
            errMsg += "(d) ServerSecret";
        } else {
            @SuppressWarnings("unused")
            String secretKey = serverSecret;
        }

        String base64PrivateKey = (String) keyVault.getItem("Pkcs8EncodedPrivateKey");
        String base64PublicKey = (String) keyVault.getItem("X509EncodedPublicKey");
        if (base64PrivateKey == null || base64PublicKey == null) {
            errMsg += "(e) Pkcs8EncodedPrivateKey or X509EncodedPublicKey";
        } else {
            List<Object> keyPair = createRSAKeyPair(base64PrivateKey, base64PublicKey);
            @SuppressWarnings("unused")
            PrivateKey privateKey = (PrivateKey) keyPair.get(0);
            @SuppressWarnings("unused")
            PublicKey publicKey = (PublicKey) keyPair.get(1);
        }

        if (errMsg != "") {
            errMsg = "Unable to get configurations from vault (" + filePath + "): " + errMsg;
            log.error(errMsg);
            SpringApplication.exit(context);
        }

    }; */

    // ************* Helper functions */

    public byte[] base64Decode(String str) {
        byte[] decoded = Base64.getDecoder().decode(str.getBytes());
        return decoded;
    }

    public String base64Encode(byte[] buf) {
        String encodedString = Base64.getEncoder().encodeToString(buf);
        return encodedString;
    }

    // NB: (a) Encrypted text is in Base64.
    // (b) Encrypted text passed for Decrypting is in Base64.
    public String randString(int len) {
        return RandomStringUtils.randomAlphanumeric(len);
    }

    /***********************
     * Other Crypto related config from property file
     **********************/
    @Value("${database.salt}")
    private String pDatabaseSalt;

    public String getSaltedCryptoKey(int len, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] byteStream = md.digest(salt.getBytes());
        String hashValue = base64Encode(byteStream);
        String encryptor = StringUtils.substring(hashValue, 2, 2 + len);
        log.info("encryptor=" + encryptor);
        return encryptor;
    }

    public String saltDecodedString(String encodedString) throws Exception {
        String text = Crypto.AESDecryptUsingKey(encodedString, getSaltedCryptoKey(32, pDatabaseSalt));
        return text;
    };

    // pass in the db password to generate the token which is then captured as
    // "token" in the property file
    public String saltEncodedString(String text) throws Exception {
        String encodedString = Crypto.AESEncryptUsingKey(text, getSaltedCryptoKey(32, pDatabaseSalt));
        return encodedString;
    }

    

    /************************ RSA related ************************/
    // Test using this site:
    // https://codepen.io/ryantriangles/pen/RyPgEW?__cf_chl_jschl_tk__=dd54d290c9c9811fbe275790ac27cd0d2f1c8e23-1600608318-0-AQF8_Ag1BIH9FBzCfPodSPY-Q-HIEPCD_AX4twDNHAVqeNISiXKl7wCsIS1wbq_X5PrQpheXwskhOn-chZv0gXdTYImRphG0TkVj4Ns3AuHCy9-a9L4FCV__a31y3rIWN7U7BUOqDr81u4WPD6ZJ1ae5kUhBo-9YbrimlehCfgpPDprEdQ2z7TLGb15GAYMj2lkdsYK_pbbLmJfnfsaE8_5T6dY-NTE1snnrp01l9AXwQ_Vn4dyUGhFNEsRIAAxM8reryDCUAJzMUnvrDR8tJ7IAOjO6JRx9GWLU4CjZ0EMSKsdN_jy5J-far40cO6tHo3Ehwvz4tvGmHNARxDQRGn4xsPW9XlE734jrKy5cuVnb

    static final int RSA_KEYLEN = 2048;
    static final String CIPHERTYPE = "RSA/ECB/PKCS1Padding";
    static final String RSA_CIPHERTYPE = "RSA/None/OAEPWITHSHA-256ANDMGF1PADDING"; // KIV

    public KeyVault getKeyVault() {
        return this.keyVault;
    }

    public List<Object> createRSAKeyPair(String base64PrivateKey, String base64PublicKey) {

        ArrayList<Object> keyPair = new ArrayList<Object>();
        if (base64PublicKey == null || base64PrivateKey == null) {
            base64PrivateKey = SecurityConstants.CRYPTO_PRIVATE_DEFAULT;
            base64PublicKey = SecurityConstants.CRYPTO_PUBLIC_DEFAULT;
        }

        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec byteKey = new PKCS8EncodedKeySpec(base64Decode(base64PrivateKey));
            PrivateKey privKey = kf.generatePrivate(byteKey);
            keyPair.add(0, privKey);
        } catch (Exception e) {
            log.error("Unable to create RSA private key from base64 value. " + e.getMessage());
            return null;
        }

        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec x509Byte = new X509EncodedKeySpec(base64Decode(base64PublicKey));
            PublicKey pubKey = kf.generatePublic(x509Byte);
            keyPair.add(1, pubKey);
        } catch (Exception e) {
            log.error("Unable to create RSA public key from base64 value. " + e.getMessage());
            return null;
        }
        return keyPair;

    }

    public Map<String, Object> generateRSAKeyPair() throws NoSuchAlgorithmException {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(RSA_KEYLEN);
        KeyPair pair = keyGen.generateKeyPair();

        PrivateKey privKey = pair.getPrivate();
        PublicKey pubKey = pair.getPublic();
        String base64PrivKey = this.base64Encode(privKey.getEncoded()); // PKCS8 std
        String base64PubKey = this.base64Encode(pubKey.getEncoded()); // X.509 std

        Map<String, Object> rsaCrypto = new HashMap<String, Object>();
        rsaCrypto.put("PrivateKey", privKey);
        rsaCrypto.put("PublicKey", pubKey);
        rsaCrypto.put("Base64PrivateKey", base64PrivKey);
        rsaCrypto.put("Base64PublicKey", base64PubKey);
        return rsaCrypto;

    };

    public String RSAEncrypt(String data) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException,
            NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(CIPHERTYPE);
        // JSENCRYPT ONLY Support RSA/ECB/PKCS1Padding
        // Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");

        List<Object> keyPair = createRSAKeyPair(CryptoKeys.getBase64PrivateKey(), CryptoKeys.getBase64PublicKey());
        cipher.init(Cipher.ENCRYPT_MODE, (PublicKey) keyPair.get(1));
        String encryptedText = Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        return encryptedText;
    }

    public String RSADecrypt(String base64Data) throws IllegalBlockSizeException, InvalidKeyException,
            BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        Cipher cipher = Cipher.getInstance(CIPHERTYPE);
        // JSENCRYPT ONLY Support RSA/ECB/PKCS1Padding
        // Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");

        List<Object> keyPair = createRSAKeyPair(CryptoKeys.getBase64PrivateKey(), CryptoKeys.getBase64PublicKey());
        cipher.init(Cipher.DECRYPT_MODE, (PrivateKey) keyPair.get(0));
        byte[] data = Base64.getDecoder().decode(base64Data.getBytes());
        String decryptedText = new String(cipher.doFinal(data));
        return decryptedText;
    }

    public static String RSAEncryptUsingX509Key(String data, String x509PublicKey)
            throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] byteKey = Base64.getDecoder().decode(x509PublicKey.getBytes());
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pKey = kf.generatePublic(x509KeySpec);
        Cipher cipher = Cipher.getInstance(CIPHERTYPE);

        cipher.init(Cipher.ENCRYPT_MODE, pKey);
        String encryptedText = Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        return encryptedText;

    }

    public static String RSADecryptUsingPKCS8Key(String base64Data, String privKey)
            throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeySpecException {

        KeyFactory kf = KeyFactory.getInstance("RSA");
        byte[] byteKey = Base64.getDecoder().decode(privKey.getBytes());
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(byteKey);
        PrivateKey pKey = kf.generatePrivate(pkcs8KeySpec);

        Cipher cipher = Cipher.getInstance(CIPHERTYPE);
        cipher.init(Cipher.DECRYPT_MODE, pKey);
        byte[] data = Base64.getDecoder().decode(base64Data.getBytes());
        String decryptedText = new String(cipher.doFinal(data));
        return decryptedText;
    }

    /************************ AES related ************************/
    private static String CIPHERSUITE = "AES/ECB/PKCS5Padding";
    private static String KEYALGO = "AES";
    private static int KEYLEN = 32;
    private static String KEYPREFIX = "PS="; // PS = Password in String

    // NB: 
    // (a) secret is a String of 32 bytes encryption/decryption key
    // (b) the key needs to be encoded in Base64 when sent to frontend js
    // (cryptojs).
    private String secretKey;

    // secretKey MUST be plain text with length 32 (AES 256)
    public void setSecretKey(String secretKey) {
        if (secretKey == null)
            this.secretKey = randString(32);
        else {
            String paddedString = StringUtils.rightPad(secretKey, 32, "0");
            this.secretKey = paddedString.substring(0, 32);
        }
    }

    public String getSecretKey() {
        if (this.secretKey == null) {
            this.secretKey = CryptoKeys.getServerSecret();
        }
        return this.secretKey;
    }

    // This is needed for Crypto.JS to create the byte array for the secret key
    public String getBase64SecretKey() {
        return base64Encode(getSecretKey().getBytes());
    }

    // Return string is a base64 encoded
    public String AESEncrypt(String Data) throws Exception {
        return AESEncryptUsingKey(Data, getSecretKey());
    }

    // data is base64 encoded
    public String AESDecrypt(String Data) throws Exception {
        return AESDecryptUsingKey(Data, getSecretKey());
    }

    // The secret in plain text and not base64 encoded.
    public static String AESEncryptUsingKey(String Data, String secret) throws Exception {

        Key key = createAESKey(secret);
        Cipher c = Cipher.getInstance(CIPHERSUITE);
        c.init(Cipher.ENCRYPT_MODE, key); // , new IvParameterSpec(iVector));
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = Base64.getEncoder().encodeToString(encVal);
        return encryptedValue;
    }

    // The secret in plain text and not base64 encoded.
    public static String AESDecryptUsingKey(String strToDecrypt, String secret) {

        try {
            Key key = createAESKey(secret);
            Cipher cipher = Cipher.getInstance(CIPHERSUITE);
            cipher.init(Cipher.DECRYPT_MODE, key);
            String plainText = new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
            return plainText;
        } catch (Exception e) {
            log.error("Uanble to decrypt : " + strToDecrypt + ". Exception:" + e.toString());
        }
        return "";
    }

    private static Key createAESKey(String secret) throws Exception {
        // byte[] decoded = Base64.getDecoder().decode(secret.getBytes());
        byte[] secretKey = secret.getBytes();
        Key key = new SecretKeySpec(secretKey, KEYALGO);
        return key;
    }

    // ****************** Related to Sealed Object *********************
    public Cipher createCipher(String secret, int cipherMode) {
        try {
            Key key = createAESKey(secret);
            Cipher cipher = Cipher.getInstance(CIPHERSUITE);
            cipher.init(cipherMode, key);
            return cipher;
        } catch (Exception e) {
            return null;
        }
        // , new IvParameterSpec(iVector));

    }

    public class KeyVault {

        private Map<String, Object> vaultMap;

        private static final String vaultHeader = "KV";
        private String vaultVersion = "1.0";
        private String vaultAuthorizationCode;
        private Date vaultCreationDate;

        public String printVaultAuthoriztionCode() {
            return "*----- Vault Authorization Code:" + this.vaultAuthorizationCode + " -----*";
        }

        public Date getVaultCreationDate() {
            return this.vaultCreationDate;
        }

        public String getVaultVersion() {
            return this.vaultVersion;
        };

        public void createVault() {
            this.vaultMap = new HashMap<String, Object>();
        }

        public boolean putItem(String key, Object vaultItem) {

            if (vaultItem == null)
                return false;

            if (vaultMap == null) {
                createVault();
            }

            if (this.vaultMap.containsKey(key)) {
                this.vaultMap.remove(key);
            }
            this.vaultMap.put(key, vaultItem);
            return true;
        }

        public Object getItem(String key) {

            if (this.vaultMap != null) {
                if (this.vaultMap.containsKey(key)) {
                    return this.vaultMap.get(key);
                }
            }
            ;
            return null;

        }

        public SealedObject sealVault(String secret) {

            Cipher cipher = createCipher(secret, Cipher.ENCRYPT_MODE);
            if (this.vaultMap != null) {
                try {
                    SealedObject sealedObject = new SealedObject((Serializable) this.vaultMap, cipher);
                    return sealedObject;

                } catch (Exception e) {
                    log.error("Unable to seal vault. " + e.toString());
                }
            }
            
            return null;
        }

        public boolean unsealVault(String secret, SealedObject sealObject) {

            Cipher cipher = createCipher(secret, Cipher.DECRYPT_MODE);
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> vaultObject = (Map<String, Object>) sealObject.getObject(cipher);
                this.vaultMap = vaultObject;
                return true;
            } catch (Exception e) {
                log.error("Unable to unseal vault. " + e.toString());
            }
            return false;
        }

        // Write vault to file
        public boolean unloadVault(String filename) throws IOException {
                       
            FileOutputStream fos ;
            ObjectOutputStream obs  ; //= null;
            try {
                

                String secret = randString(32) ;
                SealedObject sealObject = sealVault(secret) ; 
                
                if (sealObject != null) {   
                    fos = new FileOutputStream(filename) ;
                    obs = new ObjectOutputStream(fos) ;

                    Date date = new Date() ;
                    this.vaultAuthorizationCode = randString(12) ;
                    String rsaEncryptedSecret = Crypto.RSAEncryptUsingX509Key(secret, SecurityConstants.CRYPTO_VAULTPUBLIC);
                    
                    obs.writeObject(vaultHeader) ;
                    obs.writeObject(vaultVersion) ;
                    obs.writeObject(date) ;
                    obs.writeObject(this.vaultAuthorizationCode) ;   //To allow user to update or insert new key into the vault at commandline
                    obs.writeObject(rsaEncryptedSecret) ;
                    obs.writeObject(sealObject);

                   obs.close();
                   fos.close() ;
                } 
            } catch (Exception e) {
                log.error("Unable to write sealed vault to file: " + filename + " " + e.toString());
            } 
            return false;
        }

        //read vault from file
        public boolean loadVault(String filename) {

            String secret ;
            SealedObject sealObject ;
            try {
                FileInputStream fis = new FileInputStream(filename);
                ObjectInputStream ois = new ObjectInputStream(fis);

                @SuppressWarnings("unused")
                String vaultHead                = (String) ois.readObject();
                this.vaultVersion               = (String) ois.readObject();    
                this.vaultCreationDate          = (Date) ois.readObject();
                this.vaultAuthorizationCode     = (String) ois.readObject(); 
                String rsaEncryptedSecret       = (String) ois.readObject() ;
                sealObject                      = (SealedObject) ois.readObject() ;                
                
                ois.close();
                fis.close() ;
                secret = Crypto.RSADecryptUsingPKCS8Key(rsaEncryptedSecret, SecurityConstants.CRYPTO_VAULTPRIVATE) ;
                return unsealVault(secret, sealObject) ;
                 
            } catch (Exception e) {
                log.error("Unable to read from sealed vault file: " + filename + " " + e.toString());
            }
            return false;
        }

        public String vaultToJson(String authorizationCode) {

            if (this.vaultMap != null) {
                if (authorizationCode.equals(this.vaultAuthorizationCode)) {
                    try {
                        String json = new ObjectMapper().writeValueAsString(vaultMap);  
                        return json ;
                    } catch (Exception e) {
                        return "{}" ;
                    }
                }                
            } 
            return "{}";
        }

    } //KeyVault 

    void initVault(String filePath) {

        try {
            Map<String, Object> rsaKeys = this.generateRSAKeyPair() ;
            
            KeyVault kVault = new KeyVault() ;
            kVault.putItem("Pkcs8EncodedPrivateKey",rsaKeys.get("Base64PrivateKey")) ;
            kVault.putItem("X509EncodedPublicKey",rsaKeys.get("Base64PublicKey")) ;
            kVault.putItem("ServerSecret",randString(32));
            kVault.putItem("JWTSecret", randString(64)) ;
            kVault.putItem("JWTIssuer", "WebSoft") ;
            kVault.putItem("JWTVersion", "1.0") ;
            kVault.putItem("JWTDuration", 920000) ;
            kVault.unloadVault(filePath) ; 
            kVault.printVaultAuthoriztionCode();

        } catch (Exception e) {
            log.error("initVault() - Unable to initialize key vault. " + e.toString());
        }

    }
    

    // ***** Internal Testing

    public  void runTest () throws Exception {
        initVault("vault-1.dat");

    }
}