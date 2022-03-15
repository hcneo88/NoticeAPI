package org.eservice.notice.model.http;

import java.util.HashMap;
import java.util.Map;

import org.eservice.notice.constants.SecurityConstants;


public class Response {

    private Map<String, Object> responseMap ;
    private int retCode  ;
    private String retMessage  ;

    public Response() {
        responseMap = new HashMap<String, Object>();
        retMessage = "" ;
        retCode = 0 ;
    } 

    public void aspectAction() {    //Triggered by Aspect when there is a class's method annotated by 'NoEncrypt'
      
    }

    public void addItem(String key, Object value) {
        responseMap.put(key, value) ;
    }


    public void setStatus(int retCode) {
        this.retCode = retCode;
    } 

    public void setMessage(String message) {
        this.retMessage =  message ;
    }


    public Map<String, Object> getResponse(boolean disableEncrypt) {
        responseMap.put("retCode", retCode) ;
        responseMap.put("retMessage", retMessage) ;
        
        if (disableEncrypt) responseMap.put(SecurityConstants.NOENCRYPT, true);
        Map <String, Object> respMap = new HashMap<String, Object>(responseMap) ;
        
        responseMap = new HashMap<String, Object>();
        retCode = 0;
        retMessage = "";
        return respMap ;
    }
    
    public Map<String, Object> getResponse() {
        return this.getResponse(false) ;
    }

    


    public Map<String, Object> getHashMap() {
            return responseMap ;
    }
    
}
