package org.eservice.notice.component.security;

import org.eservice.notice.component.AppsConfig;
import org.eservice.notice.constants.SecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public final class CryptoKeys {

    CryptoKeys() {}

    @Autowired
    static AppsConfig appConfig ;

    public static String getBase64PublicKey() {
        String base64Key = appConfig.getString(SecurityConstants.CRYPTO_PUBLIC_KEY);
        return base64Key;
    };

    public static String getBase64PrivateKey() {
        String base64Key = appConfig.getString(SecurityConstants.CRYPTO_PRIVATE_KEY);
        return base64Key;
    };

    public static String getServerSecret() {

        String secret = appConfig.getString(SecurityConstants.CRYPTO_SECRET_KEY);
        if (secret == null) {
            return SecurityConstants.CRYPTO_SECRET_DEFAULT;
        }
        return secret;
    }
}
