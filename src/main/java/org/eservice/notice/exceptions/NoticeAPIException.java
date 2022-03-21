package org.eservice.notice.exceptions;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eservice.notice.aop.ProfilerAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import lombok.extern.slf4j.Slf4j;

public class NoticeAPIException extends Exception {

    private static final Logger logException = LoggerFactory.getLogger(NoticeAPIException.class);
    public NoticeAPIException(Exception e, String msg) {
        
        super(e) ; 
        String stacktrace = ExceptionUtils.getStackTrace(e) ;
        if (stacktrace.length() > 0 ) msg = msg + " " + stacktrace ;
        logException.error(msg) ;
    }

    public NoticeAPIException(String msg) {

        super(msg) ;         
        logException.error(msg) ;
    }
    
}
