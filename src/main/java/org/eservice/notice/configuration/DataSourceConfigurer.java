package org.eservice.notice.configuration;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.sql.DataSource;

import org.eservice.notice.component.security.Crypto;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DataSourceConfigurer {

    @Value("${database.url}")
    private String pDatabaseUrl;

    @Value("${database.driver}")
    private String pDatabaseDriver;

    @Value("${database.username}")
    private String pDatabaseUser;

    @Value("${database.salt}")
    private String pDatabaseSalt;

    @Value("${database.token}")
    private String pDatabasePassword;

    String dbPassword;

    public String getSaltedCryptoKey(int len, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] byteStream = md.digest(salt.getBytes());
        String hashValue = Base64.getEncoder().encodeToString(byteStream);
        String encryptor = StringUtils.substring(hashValue, 2, 2 + len) ;
        log.debug("Salt generated password="+encryptor);
        return encryptor;
    }

    //Have to copy this and above function from Crypto due to cyclical reference.
    public String saltDecodedString(String encodedString) throws Exception {
        String text = Crypto.AESDecryptUsingKey(encodedString, getSaltedCryptoKey(32,pDatabaseSalt)) ;
        return text ;
    } 
    

    @Bean
    public DataSource getDataSource() { 
        try {
            dbPassword = saltDecodedString(pDatabasePassword)    ;        
            var builder = DataSourceBuilder
                                        .create()
                                        .username(pDatabaseUser)
                                        .password(dbPassword)      //"dbuserpass")
                                        .driverClassName(pDatabaseDriver)
                                        .url(pDatabaseUrl)
                                        .build();
               
            return builder;  
        } catch (Exception e) {
            log.error("Unable to decode the Database password using the salt value.");
        }     
        return null;
    }
}
