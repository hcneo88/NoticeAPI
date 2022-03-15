package org.eservice.notice.testsuites;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eservice.docxapi.RecipientAddress;
import org.eservice.notice.component.NoticeAPI;
import org.eservice.notice.component.common.Util;

import org.eservice.notice.exceptions.NoticeAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class GenerateExistingNoticeTestCase {

    @Autowired 
    Util common ;

    @Autowired
    NoticeAPI noticeAPI ;

    public RecipientAddress mockRecipient() {
        RecipientAddress recipientAddress = new RecipientAddress() ;

        recipientAddress.setRecipientName("hcneo");
        recipientAddress.setAddressType('S') ;
        recipientAddress.setBlkHseNo("205") ;
        recipientAddress.setFloorNo("06");
        recipientAddress.setUnitNo("900");
        recipientAddress.setStreetName("Hougang Avenue 8");
        recipientAddress.setPostalCd("530302");
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
        String strDate = dateFormat.format(new Date());  
        recipientAddress.setTransactionDate(strDate);
        return recipientAddress;

    }

    //For QR,  string is expected in the map
    public void generateNoticeHappydayFlow(String instanceId) throws Exception {   
        
        try {
            noticeAPI.generateByInstanceId(instanceId);
        } catch (Exception e) {
            throw new NoticeAPIException(e, "Generate Notice happy day flow failed.") ;
        }
        
    }


}
