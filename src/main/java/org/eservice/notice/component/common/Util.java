package org.eservice.notice.component.common;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Util {
    @Autowired
    DataSource dataSource;

    public static final Random rnd = new Random() ; 

    public String getConnectionPoolingClass() {
        return dataSource.getClass().getName();
    }

    public static Timestamp thisMoment() {
        return new Timestamp(new Date().getTime()) ;
    }

    public static String generateID() {
      
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
        String datetime = ft.format(dNow); 
        
        int randomNumber = rnd.nextInt(99999) ;
        if( randomNumber < 10000 ) randomNumber = randomNumber + 10000;
        
        int checkDigit = ((randomNumber * 4) % 25 )  + 65 ;
        String checkAlpha = Character.toString ((char) checkDigit);  
        String idString =  datetime  + String.valueOf(randomNumber) + checkAlpha ;
        //Long id = Long.parseLong(idString) ;
        return idString ; 

    }


    public static String generateRandomDateString() {

        int year = rnd.nextInt(9) + 2021 ;
        int month = rnd.nextInt(11) + 1 ;
        int day = rnd.nextInt(27) + 1 ;

        String mString = String.valueOf(month) ;
        String dString = String.valueOf(day) ;
        if (month < 10)  
            mString = "0".concat(mString) ;
        
        if (day < 10)
            dString = "0".concat(dString) ;

        return String.valueOf(year).concat("-").concat(mString).concat("-").concat(dString) ;
    } 

}
