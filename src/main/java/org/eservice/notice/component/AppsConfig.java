package org.eservice.notice.component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

import javax.persistence.EntityManager;

import org.eservice.notice.component.common.Util;
import org.eservice.notice.model.ScConfig;
import org.eservice.notice.repository.ConfigRepository;
import org.eservice.notice.component.security.Crypto;
import org.eservice.notice.constants.SecurityConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AppsConfig {

    @Autowired
    private ApplicationContext context;
    @Autowired
    private ConfigRepository configRepo;
    @Autowired
    private Crypto crypto;
    @Autowired
    private EntityManager em;
   
   // @Autowired
   // private Util util;

    private Map<String, Object> appConfig ;
    private ScConfig ScConfig ;

    final String NO_SETTING = "Unable to find setting for configuration: " ;
    final String SYSTEM = "SYSTEM" ;

    @PostConstruct
    public void loadConfigFromDB() {
        
        boolean shutdown = false ; 
        appConfig = new HashMap<String, Object>();

        Iterable<ScConfig> itConfig = configRepo.findAll();
        for (ScConfig cfg : itConfig) {
            if (cfg.getConfigStatus().equalsIgnoreCase("A")) {  //Get only active records

                //Populate the list of URL that permit access by authenticated user regardless of role.
                //Other category of configuarations
                switch (cfg.getConfigType()) {
                    case "N" :
                        try {
                            int value = Integer.parseInt(cfg.getConfigValue()) ;
                            appConfig.put(cfg.getConfigKey(), value);
                        } catch (Exception e) {
                            appConfig.put(cfg.getConfigKey(),0);
                            log.error("Unable to convert string to int value for:" + 
                                    cfg.getConfigKey() + " " + e.toString());
                            shutdown = true;
                        }
                        break ;
                    case "S" :  
                        appConfig.put(cfg.getConfigKey(), cfg.getConfigValue());
                        break;
                    case "B" :
                        boolean value  = false;
                        if (cfg.getConfigValue().equalsIgnoreCase("true")) {
                            value = true ;
                        }
                        appConfig.put(cfg.getConfigKey(), value );
                        break;
                    case "D" : 
                        SimpleDateFormat dateFormat =new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
                        try {
                            Date date = dateFormat.parse(cfg.getConfigValue());
                            appConfig.put(cfg.getConfigKey(), date);
                        } catch (Exception e) {
                            log.error("Unable to convert string to date value for:" + 
                                    cfg.getConfigKey() + " " + e.toString());
                            shutdown = true;
                        }
                        break ;
                    case "F" :
                        float f = Float.parseFloat(cfg.getConfigValue()) ;
                        appConfig.put(cfg.getConfigKey(), f);
                        break;
                    default:
                }   

            }
        }
        
        
        if (shutdown) {
            SpringApplication.exit(context) ;
        }
    }

       
    public String getString(String key) {
        if (appConfig.containsKey(key)) {
            return (String) appConfig.get(key) ;
        }
        return null; 
    }

    public int getInt(String key) {
      
        int value = 0 ;
        try {
            if (appConfig.containsKey(key)) {  
                value = (int) appConfig.get(key) ;
            } else {
                log.error( NO_SETTING + key) ;
            }
        } catch (Exception ex) {
            log.error("getInt(): Not getting an integer value. " + ex.toString()) ;
        }
        return value ;
    }

    public Date getDate(String key) {
        if (appConfig.containsKey(key)) {
            return (Date) appConfig.get(key) ;
        }
        log.error(NO_SETTING + key) ;
        return null; 
    }

    public float getDecimal(String key) {
        if (appConfig.containsKey(key)) {
            
            return (float) appConfig.get(key) ;
        }
        log.error(NO_SETTING + key) ;
        return 0; 
    }

    public boolean getBoolean(String key) {
        if (appConfig.containsKey(key)) {
            return (boolean) appConfig.get(key) ;
        }
        log.error(NO_SETTING + key) ;
        return false; 
    }

    //specific to Transmogrifer only !
    public boolean requireGlobalEncryption() {
        loadConfigFromDB();
        return getBoolean("transmogrifier.securedata");
    } 

    //specific to JWT
    public byte[] getJwtSecret() {
        String secret = getString(SecurityConstants.JWT_TOKEN_KEY);
        if (secret == null) {
            return SecurityConstants.JWT_TOKEN_DEFAULT.getBytes();
        }
        return secret.getBytes();
    }

    public String getJwtIssuer() {
        String issuer = getString(SecurityConstants.JWT_ISSUER_KEY);
        if (issuer == null) {
            return SecurityConstants.JWT_ISSUER_DEFAULT;
        }
        return issuer;
    }

    public String getJwtVersion() {
        String version = getString(SecurityConstants.JWT_VERSION_KEY);
        if (version == null) {
            return SecurityConstants.JWT_VERSION_DEFAULT;
        }
        return version;
    }

    public int getJwtDuration() {
        int value = getInt(SecurityConstants.JWT_DURATION_KEY);
        if (value == 0) {
            return SecurityConstants.JWT_DURATION_DEFAULT;
        }
        return value;
    }

    public String initConfig() {

        Timestamp now = new Timestamp(new Date().getTime());

        ScConfig = new ScConfig();
        ScConfig.setConfigId(Util.generateID());
        ScConfig.setConfigKey("crypto.serversecret");          //server AES key
        ScConfig.setConfigValue(crypto.randString(32));    
        ScConfig.setConfigType("S");
        ScConfig.setUpdatedBy(SYSTEM);
        ScConfig.setCreatedBy(SYSTEM);
        ScConfig.setUpdatedDate(now);
        ScConfig.setCreatedDate(now);
        configRepo.save(ScConfig);

        ScConfig = new ScConfig();
        ScConfig.setConfigId(Util.generateID());
        ScConfig.setConfigKey("crypto.jwttoken");              //JWT Signing key
        ScConfig.setConfigValue(crypto.randString(64));
        ScConfig.setConfigType("S");
        ScConfig.setUpdatedBy(SYSTEM);
        ScConfig.setCreatedBy(SYSTEM);
        ScConfig.setUpdatedDate(now);
        ScConfig.setCreatedDate(now);
        configRepo.save(ScConfig);

        ScConfig = new ScConfig();
        ScConfig.setConfigId(Util.generateID());
        ScConfig.setConfigKey("crypto.jwtversion");            //JWT Version
        ScConfig.setConfigValue("1.0");
        ScConfig.setConfigType("S");
        ScConfig.setUpdatedBy(SYSTEM);
        ScConfig.setCreatedBy(SYSTEM);
        ScConfig.setUpdatedDate(now);
        ScConfig.setCreatedDate(now);
        configRepo.save(ScConfig);

        ScConfig = new ScConfig();
        ScConfig.setConfigId(Util.generateID());
        ScConfig.setConfigKey("crypto.jwtissuer");             //JWT Issuer
        ScConfig.setConfigValue("WebSoft");
        ScConfig.setConfigType("S");
        ScConfig.setUpdatedBy(SYSTEM);
        ScConfig.setCreatedBy(SYSTEM);
        ScConfig.setUpdatedDate(now);
        ScConfig.setCreatedDate(now);
        configRepo.save(ScConfig);

        ScConfig = new ScConfig();
        ScConfig.setConfigId(Util.generateID());
        ScConfig.setConfigKey("crypto.jwtduration");           //Valdity duration for JWT token
        ScConfig.setConfigValue("900000");
        ScConfig.setConfigType("N");
        ScConfig.setUpdatedBy(SYSTEM);
        ScConfig.setCreatedBy(SYSTEM);
        ScConfig.setUpdatedDate(now);
        ScConfig.setCreatedDate(now);
        configRepo.save(ScConfig);

        try {
            Map<String, Object> rsaKeys = crypto.generateRSAKeyPair() ;
            ScConfig = new ScConfig();
            ScConfig.setConfigId(Util.generateID());
            ScConfig.setConfigKey("crypto.public");
            ScConfig.setConfigValue((String)rsaKeys.get("Base64PublicKey"));   //Server public RSA key
            ScConfig.setConfigType("S");
            ScConfig.setUpdatedBy(SYSTEM);
            ScConfig.setCreatedBy(SYSTEM);
            ScConfig.setUpdatedDate(now);
            ScConfig.setCreatedDate(now);
            configRepo.save(ScConfig);
            
            ScConfig = new ScConfig();
            ScConfig.setConfigId(Util.generateID());
            ScConfig.setConfigKey("crypto.private");
            ScConfig.setConfigValue((String)rsaKeys.get("Base64PrivateKey"));  //Server private RSA key
            ScConfig.setConfigType("S");
            ScConfig.setUpdatedBy(SYSTEM);
            ScConfig.setCreatedBy(SYSTEM);
            ScConfig.setUpdatedDate(now);
            ScConfig.setCreatedDate(now);
            configRepo.save(ScConfig);
        } catch (Exception e) {
            log.error("Unable to setup private/public key in database. Exception:" + e.toString());
        }

        // Client side encryption required ?
        ScConfig = new ScConfig();
        ScConfig.setConfigId(Util.generateID());
        ScConfig.setConfigKey("accessManagementController.ClientEncryptionOption");          //server AES key
        ScConfig.setConfigValue("true");    
        ScConfig.setConfigType("B");
        ScConfig.setUpdatedBy(SYSTEM);
        ScConfig.setCreatedBy(SYSTEM);
        ScConfig.setUpdatedDate(now);
        ScConfig.setCreatedDate(now);
        configRepo.save(ScConfig);

        // client permissible idle time and session timeout after idle time
        ScConfig = new ScConfig();
        ScConfig.setConfigId(Util.generateID());
        ScConfig.setConfigKey("accessManagementController.idleTime");          //server AES key
        ScConfig.setConfigValue("3");  //set to 3 minutes    
        ScConfig.setConfigType("N");
        ScConfig.setUpdatedBy(SYSTEM);
        ScConfig.setCreatedBy(SYSTEM);
        ScConfig.setUpdatedDate(now);
        ScConfig.setCreatedDate(now);
        configRepo.save(ScConfig);

        ScConfig = new ScConfig();
        ScConfig.setConfigId(Util.generateID());
        ScConfig.setConfigKey("accessManagementController.timeout");         
        ScConfig.setConfigValue("1");   //1 minute from idle time if there is no activity.   
        ScConfig.setConfigType("N");
        ScConfig.setUpdatedBy(SYSTEM);
        ScConfig.setCreatedBy(SYSTEM);
        ScConfig.setUpdatedDate(now);
        ScConfig.setCreatedDate(now);
        configRepo.save(ScConfig);

        //server side encryption required ?  
        ScConfig = new ScConfig();
        ScConfig.setConfigId(Util.generateID());
        ScConfig.setConfigKey("transmogrifier.securedata");          
        ScConfig.setConfigValue("true");    
        ScConfig.setConfigType("B");
        ScConfig.setUpdatedBy(SYSTEM);
        ScConfig.setCreatedBy(SYSTEM);
        ScConfig.setUpdatedDate(now);
        ScConfig.setCreatedDate(now);
        configRepo.save(ScConfig);

        String appsKeys = "Initialized application settings:\r" ;
        Iterable<ScConfig> itConfig = configRepo.findAll();
        for (ScConfig prop : itConfig) {
            appsKeys += prop.getConfigKey();
            log.debug("Config key:" + prop.getConfigKey()) ;
        }
        return appsKeys;

    }


}
