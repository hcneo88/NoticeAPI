package org.eservice.notice.testsuites;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eservice.docxapi.RecipientAddress;
import org.eservice.notice.component.NoticeAPI;
import org.eservice.notice.component.common.Util;
import org.eservice.notice.constants.NoticeNumEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CreateNoticeTestCase {

    @Autowired
    NoticeAPI noticeAPI ;

    @Autowired
    Util commonService;

    public void happydayFlowType3() {  //3 : Got valid name and address 

        try {
            RecipientAddress recipientAddress = new RecipientAddress() ;            
            recipientAddress.setAddressType('S') ;
            recipientAddress.setBlkHseNo("205") ;
            recipientAddress.setFloorNo("06");
            recipientAddress.setUnitNo("900");
            recipientAddress.setStreetName("Hougang Avenue 8");
            recipientAddress.setPostalCd("530302");
            recipientAddress.setRecipientName("hcneo");
            
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
            String strDate = dateFormat.format(new Date());  
            recipientAddress.setTransactionDate(strDate);

            noticeAPI.createNotice(NoticeNumEnum.UC0101N001, recipientAddress) ;
            log.info("Happy day flow passed.");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void exceptionFlowType3A() {  //3 : Expect valid name and address but name is not provided 

        try {
            RecipientAddress recipientAddress = new RecipientAddress() ;
            recipientAddress.setAddressType('S') ;
            recipientAddress.setBlkHseNo("205") ;
            recipientAddress.setFloorNo("06");
            recipientAddress.setUnitNo("900");
            recipientAddress.setStreetName("Hougang Avenue 8");
            recipientAddress.setPostalCd("530302");

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
            String strDate = dateFormat.format(new Date());  
            recipientAddress.setTransactionDate(strDate);
             
            // recipientAddress.setRecipientName("hcneo");
            noticeAPI.createNotice(NoticeNumEnum.UC0101N001, recipientAddress) ;
        } catch (Exception e) {
            log.error("Exception flow 3A failed:" + e.getMessage());                 
        }
    }

    public void exceptionFlowType3B() {  //3 : Expect valid name and address but address is not provided 

        try {
            RecipientAddress recipientAddress = new RecipientAddress() ;
            //recipientAddress.setAddressType('S') ;
            //recipientAddress.setBlkHseNo("205") ;
            //recipientAddress.setFloorNo("06");
            //recipientAddress.setUnitNo("900");
            //recipientAddress.setStreetName("Hougang Avenue 8");
            //recipientAddress.setPostalCd("530302");

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
            String strDate = dateFormat.format(new Date());  
            recipientAddress.setTransactionDate(strDate);
            recipientAddress.setRecipientName("hcneo");
            noticeAPI.createNotice(NoticeNumEnum.UC0101N001, recipientAddress) ;
        } catch (Exception e) {
            log.error("Exception flow 3B failed:" + e.getMessage());  
        }
    }

    //No need to run the below.  Result will be similar to 3B because name is checked first !
    public void exceptionFlowType3C() {  //3 : Expect valid name and address but name and address are not provided 

        try {
            RecipientAddress recipientAddress = new RecipientAddress() ;
            //recipientAddress.setAddressType('S') ;
            //recipientAddress.setBlkHseNo("205") ;
            //recipientAddress.setFloorNo("06");
            //recipientAddress.setUnitNo("900");
            //recipientAddress.setStreetName("Hougang Avenue 8");
            //recipientAddress.setPostalCd("530302");
            // recipientAddress.setRecipientName("hcneo");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
            String strDate = dateFormat.format(new Date());  
            recipientAddress.setTransactionDate(strDate);
            noticeAPI.createNotice(NoticeNumEnum.UC0101N001, recipientAddress) ;
        } catch (Exception e) {
            log.error("Exception flow 3C failed:" + e.getMessage());  
        }
    }



}
