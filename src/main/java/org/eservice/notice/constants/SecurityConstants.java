package org.eservice.notice.constants;

public final class SecurityConstants {

    public static final String SYSTEM_SEED = "SeEd@n0T1cEAP1";  //DO NOT CHANGE
    public static final String AUTH_LOGIN_URL = "/api/authenticate";

    // Signing key for HS512 algorithm
    // Use the page http://www.allkeysgenerator.com/ to generate all kinds of keys
    public static final String JWT_SECRET = "n2r5u8x/A%D*G-KaPdSgVkYp3s6v9y$B&E(H+MbQeThWmZq4t7w!z%C*F-J@NcRf";
    

    // JWT token defaults
    public static final String  TOKEN_HEADER = "Authorization";
    public static final String  TOKEN_PREFIX = "Bearer ";
    public static final String  TOKEN_TYPE = "JWT";
    public static final int     TOKEN_DURATION = 900000 ;
    public static final String  TOKEN_SESSIONID = "SessionToken" ;
    public static final boolean TOKEN_AUTOREFRESH = false ;   //use in JwtAuthorization to determine if jwt is regenerated per request
    
    public static final String CLIENT_RSAENCRYPTEDKEY = "Client-rKey" ; //client AES key encrypted by server public RSA key
    public static final String CLIENT_AESENCRYPTEDKEY = "Client-aKey" ; //client AES key encrypted by server AES key
    public static final String CLIENT_ENCRYPTIONKEY = "Client-eKey" ;   //client AES key (in clear)
        
    public static final String USERNAME = "username" ;
    public static final String ENCRYPTED_PAYLOAD = "encrypted-data" ;   //the json fieldname that indicates encrypted payload
    public static final String NOENCRYPT = "no-encrypt" ;

    public static final String JWT_TOKEN_DEFAULT = "n2r5u8x/A%D*G-KaPdSgVkYp3s6v9y$B&E(H+MbQeThWmZq4t7w!z%C*F-J@1234";
    public static final String JWT_TOKEN_KEY = "crypto.jwttoken" ;
    
    public static final String JWT_ISSUER_DEFAULT = "Default Issuer" ;
    public static final String JWT_ISSUER_KEY = "crypto.jwtissuer" ;

    public static final String JWT_VERSION_DEFAULT = "Default Version" ;
    public static final String JWT_VERSION_KEY = "crypto.jwtversion" ;

    public static final int JWT_DURATION_DEFAULT = 920000;
    public static final String JWT_DURATION_KEY = "crypto.jwtduration" ;

    public static final String CRYPTO_SECRET_DEFAULT = "bXVz_GJlMzJieXRlc2tleW11c3R71Z11" ;
    public static final String CRYPTO_SECRET_KEY = "crypto.serversecret" ;

    //Use https://www.devglan.com/online-tools/rsa-encryption-decryption to generate RSA keys
    public static final String CRYPTO_PRIVATE_DEFAULT= "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCTGqI9rqd/A5/wBk/l7dzBDNJ4VZ9PSmRlnmaTpweHnIK9rQZj23QMa34ht/aiv3Q3sdJNzF43OEPd2gAwKjkKEzAF0Z40LLg6Sv61Mndg0JWhLhqsdV78mLjiHNbJU1YFxIpXTJH/7CAWxIAe5aYt/aCMWjJa2GU+2Op7yR0d4fVW1OLZXAmfHPOsvqc4nXzoLK+ZN20E7iVtVMmB6vn9jDZa3gj+cED3BA6UB18r9b/2j5rOTZaWh4cSmftcLZXwcJdLaOOp8mTybgUjpzK3inavmvU+Gcet1R0PBzux+foNEAdjcTb5kBDEeIRT+qSFsIa0ae1jTldZRM65YMenAgMBAAECggEAKbj4Iz6SSzgf+NgReUmJv0d/upmuYbLb3uaaeW56eEAv5NRaZ6AqvgsO70laLrbWo6VP3LkGT+spdJcMTpzrKKxghoX5tRqp9f6nV4VEzrTae6iz8A4qLsKyTY+Ya+mzEFe6VSBmmIvEXjDFlf3UsF6rVqOOfK27qaOkfg+ArqSlqmmoS4xkMZmhDm3ZwD4KA/LwL+55JYLteLVYUnarlM4h8hljHfICsPJZFbxrEIEm7f13G6BHEa9yfoRB3Fwrxnjg6m7bjsX6hEgE+WpqIHfeTIUFeamFOKJT4UclXuAaZwJpeG2gaQzWTOiKA3hpEONIHmt2Pgu0t2pOXawboQKBgQDRCh+Y0soG/96RZ1mMRhGYxlRdV43S6ECNYsXGYtp8NNuwDkgx85MhH+ruF0QwhOO7+HVtUEHW+rP1hLKRebW2EFMI9ZrhBAwn/APDdIPdyBQ2uHsqkOMODJjOp4ZEXITm0G6NVAqaHGT0fvKXn+NWQ7w8UeR8pYtY2CYCdP4OrQKBgQC0Jph+yNxiZRlv/M1jB9BWB43kz0KuKiyInwpl5ivJmBPB9SrzM8eTYHoAQkq++VDTQBgDnAXfM/LNPnt9DBZ5icgQ2el49Ri8vRZWmSpx0UzzKl3ZkaC7eSMDYMvZ42b0Zigp3bkDyfrqjpZpQqD3a0Q2FqtHEq69A+pwRK6eIwKBgGAh8rP6RFSBuR5PVBwxYQhMNSIUELHsgztCMEfy0B2MxXcqqkLmjEQQhJ7n7kEN7BlWwtH5tb2i373KP8CtI0bOLRGSuZ4/mUOOH0D0xWvqBnm/z0ydAv5EBsYKvrGThr8LHbw66QFEi5zxAKmAKzFRxzBKTPSRwQqjpJ8+pFOlAoGATyZt9bZWbbyxkmxl6M60EDg9dXxCdbb93gDSVrbsIiR4+cy31Ca51tyafaaipIlxo38TjfJs+gYe9WIwa152OWv5xL2ZQJWfCr2hJOciqOMmheflWAQNtJCie4d5yP+KanK/zrUl5q5FWezP1Ot7QXmVfJY1JoLk1xLzW+QxPDUCgYBaHJb8suksC4+JEyuwkT/6pdr3LKn3AhnImEKNPYWm52Sfff9EPKDyZiNaNDchho9YxkQd6j66Amfq2UypsMuSQWh7bEf78tcpQP5WbfsfSUCOYpRWXakSjGcX/Jv96FQxHJdFAf6kEk/irX+wIfBHqxc3nXwzrLU/+LTKG7c6EQ==";
    public static final String CRYPTO_PRIVATE_KEY="crypto.private" ; 

    public static final String CRYPTO_PUBLIC_DEFAULT= "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkxqiPa6nfwOf8AZP5e3cwQzSeFWfT0pkZZ5mk6cHh5yCva0GY9t0DGt+Ibf2or90N7HSTcxeNzhD3doAMCo5ChMwBdGeNCy4Okr+tTJ3YNCVoS4arHVe/Ji44hzWyVNWBcSKV0yR/+wgFsSAHuWmLf2gjFoyWthlPtjqe8kdHeH1VtTi2VwJnxzzrL6nOJ186CyvmTdtBO4lbVTJger5/Yw2Wt4I/nBA9wQOlAdfK/W/9o+azk2WloeHEpn7XC2V8HCXS2jjqfJk8m4FI6cyt4p2r5r1PhnHrdUdDwc7sfn6DRAHY3E2+ZAQxHiEU/qkhbCGtGntY05XWUTOuWDHpwIDAQAB"; 
    public static final String CRYPTO_PUBLIC_KEY="crypto.public" ; 

    //The below CANNOT be changed because of its special use to encrypt the AES key to seal objects in key vault 
    public static final String CRYPTO_VAULTPUBLIC = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkxqiPa6nfwOf8AZP5e3cwQzSeFWfT0pkZZ5mk6cHh5yCva0GY9t0DGt+Ibf2or90N7HSTcxeNzhD3doAMCo5ChMwBdGeNCy4Okr+tTJ3YNCVoS4arHVe/Ji44hzWyVNWBcSKV0yR/+wgFsSAHuWmLf2gjFoyWthlPtjqe8kdHeH1VtTi2VwJnxzzrL6nOJ186CyvmTdtBO4lbVTJger5/Yw2Wt4I/nBA9wQOlAdfK/W/9o+azk2WloeHEpn7XC2V8HCXS2jjqfJk8m4FI6cyt4p2r5r1PhnHrdUdDwc7sfn6DRAHY3E2+ZAQxHiEU/qkhbCGtGntY05XWUTOuWDHpwIDAQAB";
    public static final String CRYPTO_VAULTPRIVATE = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCTGqI9rqd/A5/wBk/l7dzBDNJ4VZ9PSmRlnmaTpweHnIK9rQZj23QMa34ht/aiv3Q3sdJNzF43OEPd2gAwKjkKEzAF0Z40LLg6Sv61Mndg0JWhLhqsdV78mLjiHNbJU1YFxIpXTJH/7CAWxIAe5aYt/aCMWjJa2GU+2Op7yR0d4fVW1OLZXAmfHPOsvqc4nXzoLK+ZN20E7iVtVMmB6vn9jDZa3gj+cED3BA6UB18r9b/2j5rOTZaWh4cSmftcLZXwcJdLaOOp8mTybgUjpzK3inavmvU+Gcet1R0PBzux+foNEAdjcTb5kBDEeIRT+qSFsIa0ae1jTldZRM65YMenAgMBAAECggEAKbj4Iz6SSzgf+NgReUmJv0d/upmuYbLb3uaaeW56eEAv5NRaZ6AqvgsO70laLrbWo6VP3LkGT+spdJcMTpzrKKxghoX5tRqp9f6nV4VEzrTae6iz8A4qLsKyTY+Ya+mzEFe6VSBmmIvEXjDFlf3UsF6rVqOOfK27qaOkfg+ArqSlqmmoS4xkMZmhDm3ZwD4KA/LwL+55JYLteLVYUnarlM4h8hljHfICsPJZFbxrEIEm7f13G6BHEa9yfoRB3Fwrxnjg6m7bjsX6hEgE+WpqIHfeTIUFeamFOKJT4UclXuAaZwJpeG2gaQzWTOiKA3hpEONIHmt2Pgu0t2pOXawboQKBgQDRCh+Y0soG/96RZ1mMRhGYxlRdV43S6ECNYsXGYtp8NNuwDkgx85MhH+ruF0QwhOO7+HVtUEHW+rP1hLKRebW2EFMI9ZrhBAwn/APDdIPdyBQ2uHsqkOMODJjOp4ZEXITm0G6NVAqaHGT0fvKXn+NWQ7w8UeR8pYtY2CYCdP4OrQKBgQC0Jph+yNxiZRlv/M1jB9BWB43kz0KuKiyInwpl5ivJmBPB9SrzM8eTYHoAQkq++VDTQBgDnAXfM/LNPnt9DBZ5icgQ2el49Ri8vRZWmSpx0UzzKl3ZkaC7eSMDYMvZ42b0Zigp3bkDyfrqjpZpQqD3a0Q2FqtHEq69A+pwRK6eIwKBgGAh8rP6RFSBuR5PVBwxYQhMNSIUELHsgztCMEfy0B2MxXcqqkLmjEQQhJ7n7kEN7BlWwtH5tb2i373KP8CtI0bOLRGSuZ4/mUOOH0D0xWvqBnm/z0ydAv5EBsYKvrGThr8LHbw66QFEi5zxAKmAKzFRxzBKTPSRwQqjpJ8+pFOlAoGATyZt9bZWbbyxkmxl6M60EDg9dXxCdbb93gDSVrbsIiR4+cy31Ca51tyafaaipIlxo38TjfJs+gYe9WIwa152OWv5xL2ZQJWfCr2hJOciqOMmheflWAQNtJCie4d5yP+KanK/zrUl5q5FWezP1Ot7QXmVfJY1JoLk1xLzW+QxPDUCgYBaHJb8suksC4+JEyuwkT/6pdr3LKn3AhnImEKNPYWm52Sfff9EPKDyZiNaNDchho9YxkQd6j66Amfq2UypsMuSQWh7bEf78tcpQP5WbfsfSUCOYpRWXakSjGcX/Jv96FQxHJdFAf6kEk/irX+wIfBHqxc3nXwzrLU/+LTKG7c6EQ==";
    
    //Change in any of the values requires constants in Angular to be changed too
    public static final int LOGIN_UNKNOWNERROR      =  900000 ;
    public static final int LOGIN_DUPLICATE         =  999999 ;
    public static final int LOGIN_TERMINATED        =  999989 ;
    public static final int LOGIN_SUSPENDED         =  999979 ;  //login but suspended ?
    public static final int LOGIN_EXCESSIVEFAILED   =  999969 ;  //excessive failed authentication 
    public static final int LOGIN_BADCREDENTIAL     =  999959 ;
    public static final int LOGIN_PASSWORDCHANGE    =  888888 ;  //force login
    public static final int LOGIN_SUCCESSFUL        =  0 ;
    //
    public static final int ACCESS_NOPERMISSION    =  999949 ; //No granted function
    public static final int ACCESS_DENIED          =  999939 ; //Trying to access a protected API or resource
    public static final int ACCESS_CRYPTOISSUE     =  999929 ;  
    public static final int ACCESS_FAILEDLOGIN     =  999919 ;
    //
    public static final int ACCESS_JWTEXPIRED      =  999910 ;
    public static final int ACCESS_JWTRELATED      =  999909 ;
    public static final int ACCESS_INVALIDATE      =  999908 ;  //Detected another sign-on session.  Ask to terminate current one
    

    public static final String ACCOUNT_ACTIVE_CD = "A" ;
    public static final String ACCOUNT_SUSPENDED_CD = "S" ;
    public static final String ACCCOUNT_TERMINATED_CD = "T";

    //TODO Change to configuration
    public static final int ACCOUNT_PASSWORDINTERVAL = 90 ;   //Prompt to change password after x days

    public static final boolean SECURE_DATA = true ;

    private SecurityConstants() {
        throw new IllegalStateException("Cannot create instance of static util class");
    }
}
