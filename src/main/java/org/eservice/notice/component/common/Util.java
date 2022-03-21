package org.eservice.notice.component.common;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.sql.DataSource;

import com.github.javafaker.Faker;

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

    public static String generateFakerData(char fieldType) {

        Faker dataFaker = new Faker();
        int choice = rnd.nextInt(5) ;
        int number = rnd.nextInt(1000) ;

        if (fieldType == 'N') 
            return String.valueOf(dataFaker.number().numberBetween(number,number+5000)) ;
        if (fieldType == 'F') 
            return String.valueOf(dataFaker.number().randomDouble(2, 100, 1000));
        if (fieldType == 'D') 
            return Util.generateRandomDateString().concat(" 10:11:12") ;

        String strValue = "" ;    
        switch (choice) {
            case 1  :   strValue = dataFaker.ancient().primordial() ;
                        break ;
            case 2  :   strValue = dataFaker.animal().name() ;
                        break;
            case 3  :   strValue = dataFaker.app().name() ;
                        break;
            case 4  :   strValue = dataFaker.beer().name() ;
                        break;
            case 5  :   strValue = dataFaker.book().title() ;
                        break ;
            default :   strValue = dataFaker.commerce().productName();
        }    
        return strValue ;
    }

}
