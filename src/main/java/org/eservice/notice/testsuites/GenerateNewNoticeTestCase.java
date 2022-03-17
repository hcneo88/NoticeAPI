package org.eservice.notice.testsuites;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eservice.docxapi.Crypto;
import org.eservice.docxapi.RecipientAddress;
import org.eservice.notice.component.NoticeAPI;
import org.eservice.notice.component.common.Util;
import org.eservice.notice.constants.NoticeNumEnum;
import org.eservice.notice.exceptions.NoticeAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//@Slf4j
@Service
public class GenerateNewNoticeTestCase {

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
    public void simpleFieldsHappydayFlow() throws Exception {   

        RecipientAddress recipientAddress = mockRecipient(); 
        Map<String, Object> textOrImageFields = new HashMap<>();
        textOrImageFields.put("regdate",noticeAPI.createTextField("2022-03-09 23:00:00")) ;
        textOrImageFields.put("arfamount",noticeAPI.createTextField("100.30")) ;
        textOrImageFields.put("validityperiod",noticeAPI.createTextField("30")) ;
        textOrImageFields.put("apptdate",noticeAPI.createTextField("30 March 2022")) ;
        textOrImageFields.put("qrtransactioncode","Ref12345") ;
        textOrImageFields.put("image", noticeAPI.createImageField(noticeAPI.getTemplatePath() + "/images/merlion.jpg")) ;

        try {
            noticeAPI.createNotice(NoticeNumEnum.UC0101N001, recipientAddress) ;
            noticeAPI.populateMergeFields(textOrImageFields);
            noticeAPI.dbUploadFields() ;
            noticeAPI.generateNotice() ;
        } catch (Exception e) {
            throw new NoticeAPIException(e, "Happyday flow failed.") ;

        }
        
    }

    public void simpleFieldsExceptionFlowA() throws Exception {   

        RecipientAddress recipientAddress = mockRecipient(); 
        Map<String, Object> textOrImageFields = new HashMap<>();
        textOrImageFields.put("regdate",noticeAPI.createTextField("2022-13-09 23:00:00")) ; //<= Invalid date
        textOrImageFields.put("arfamount",noticeAPI.createTextField("100.30")) ;            
        textOrImageFields.put("validityperiod",noticeAPI.createTextField("ZY")) ;           //<= text instead of numberic
        textOrImageFields.put("apptdate",noticeAPI.createTextField("30 March 2022")) ;      
        textOrImageFields.put("qrtransactioncode","Ref12345") ;
        textOrImageFields.put("image", noticeAPI.createImageField(noticeAPI.getTemplatePath() + "/images/merlion.jpg")) ;

        try {
            noticeAPI.createNotice(NoticeNumEnum.UC0101N001, recipientAddress) ;
            noticeAPI.populateMergeFields(textOrImageFields);
            noticeAPI.dbUploadFields() ;
            noticeAPI.generateNotice() ;
        } catch (Exception e) {
            throw new NoticeAPIException(e, "Failed as expected.") ;
        }
        
    }

    public void tableFieldsHappydayFlow() throws Exception {   

        RecipientAddress recipientAddress = mockRecipient(); 

        try {
            noticeAPI.createNotice(NoticeNumEnum.UC0101N003, recipientAddress) ;
            for (int r=0;r<50;r++) {
                Map<String, String> columnForOneRow = new HashMap<>() ;
                columnForOneRow.put("sku", Crypto.getRandString(8));
                columnForOneRow.put("qty", String.valueOf((r+1)*2)); 
                columnForOneRow.put("value", String.valueOf(7*((r+1)*1.5)));
                columnForOneRow.put("validity", Util.generateRandomDateString());
                noticeAPI.populateTableField("TableA", columnForOneRow);
            }
            
            noticeAPI.dbUploadFields() ;
            noticeAPI.generateNotice() ;
        } catch (Exception e) {
            throw new NoticeAPIException(e, "Happyday flow failed.") ;
        }
        
    }


}
