package org.eservice.notice.controller;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eservice.docxapi.DocXAPI;
import org.eservice.docxapi.RecipientAddress;
import org.eservice.notice.component.NoticeAPI;
import org.eservice.notice.constants.ErrorMessage;
import org.eservice.notice.constants.NoticeNumEnum;
import org.eservice.notice.exceptions.NoticeAPIException;
import org.eservice.notice.model.http.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class GenerateNotice extends Response {


    @Autowired
    NoticeAPI noticeAPI ;

    @PostMapping("/notice/generate/pdf")
    @SuppressWarnings("unchecked") 
    public ResponseEntity<byte[]> generatePdf(@RequestBody Map<String, Object> param) {
        
        try {
            String noticeNumber = (String)param.get("noticeNumber") ;
            log.debug("NOTICE #{}", noticeNumber);
            
            Map <String, String> rAddr = (Map<String, String>) param.get("recipientAddress") ;
            if (rAddr == null) {
                throw new NoticeAPIException(ErrorMessage.E1009 + noticeNumber) ;
            }
            RecipientAddress recipientAddress = new RecipientAddress() ;
            recipientAddress.setRecipientName(rAddr.get("recipientName"));
            recipientAddress.setAddressType(rAddr.get("addressType").charAt(0));
            recipientAddress.setBlkHseNo(rAddr.get("blkHseNo"));
            recipientAddress.setBuildingName(rAddr.get("buildingName"));
            recipientAddress.setFloorNo(rAddr.get("floorNo"));
            recipientAddress.setUnitNo(rAddr.get("unitNo"));
            recipientAddress.setStreetName(rAddr.get("streetName"));
            recipientAddress.setPostalCd(rAddr.get("postalCd"));
            recipientAddress.setTransactionDate(rAddr.get("transactionDate"));
            
            Map<String, String> textFields = (Map<String, String>) param.get("textFields") ; 
            Map<String, byte[]> imageFields = (Map<String, byte[]>) param.get("imageFields") ; 
            Map<String, String> qrTextFields = (Map<String, String>) param.get("qrTextFields") ; 
            Map<String, List<Map<String, String>>> dataTables = (Map<String, List<Map<String, String>>>) param.get("dataTables") ;

            Map<String, Object> textOrImageFields = new HashMap<>();
            if (textFields != null && textFields.size() > 0) {
                for (Map.Entry<String, String> m : textFields.entrySet()) {
                    textOrImageFields.put(m.getKey(),DocXAPI.createTextField(m.getValue()));
                }
            }   
            
            if (imageFields != null && imageFields.size() > 0) {
                for (Map.Entry<String, byte[]> m : imageFields.entrySet()) {
                    textOrImageFields.put(m.getKey(),DocXAPI.createImageField(m.getValue()));
                }
            }

            if (qrTextFields != null && qrTextFields.size() > 0) {
                for (Map.Entry<String, String> m : qrTextFields.entrySet()) {
                    textOrImageFields.put(m.getKey(), m.getValue());
                }
            }

            noticeAPI.createNotice(NoticeNumEnum.valueOf(noticeNumber), recipientAddress);
            noticeAPI.populateMergeFields(textOrImageFields);
            
            //Handle tables
            if (dataTables != null && dataTables.size() > 0 ) {

                for (String eachTableName: dataTables.keySet()) {  //Get list of rows for the specific table
                    List<Map<String, String>> rowList = dataTables.get(eachTableName) ;
                    log.debug("Table rows: {}", rowList) ;
                    if (rowList.size() > 0) {
                        for (Map<String, String> columnDataForOneRow : rowList) {
                            noticeAPI.populateTableField(eachTableName, columnDataForOneRow) ;
                        }
    
                    }
                }
            } 
            
            noticeAPI.allFieldsReady() ;   //Final check and confirm no errors on merge or table fields.
            noticeAPI.dbUploadFields();    //Wont execute if allFieldsReady throws exception
            ByteArrayOutputStream bos = noticeAPI.generate();   //If either mergeFieldErrors or tableFieldErrors are set, exception will be thrown

            HttpHeaders headers = new HttpHeaders();
            ContentDisposition contentDisposition = 
                        ContentDisposition.builder("inline").filename(noticeNumber+"-" + 
                                                                      recipientAddress.getTransactionId() +
                                                                      ".pdf").build();
           
            headers.setContentType(MediaType.parseMediaType("application/pdf"));
            headers.setContentDisposition(contentDisposition);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(bos.toByteArray(), headers, HttpStatus.OK);
            return response; 

        } catch (Exception e) {
                log.error(e.toString()) ;
                return ResponseEntity.badRequest().body(e.getMessage().getBytes()) ;
        }                  

    }

    @GetMapping("/notice/regenerate/pdf")
    public ResponseEntity<byte[]> regeneratePdf(@RequestParam String instanceId) {
        
        try {
            log.info("NOTICE # {}", instanceId) ;
            ByteArrayOutputStream bos = noticeAPI.generateByInstanceId(instanceId);

            HttpHeaders headers = new HttpHeaders();
            ContentDisposition contentDisposition = 
                        ContentDisposition.builder("inline").filename(instanceId+".pdf").build();
           
            headers.setContentType(MediaType.parseMediaType("application/pdf"));
            headers.setContentDisposition(contentDisposition);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(bos.toByteArray(), headers, HttpStatus.OK);
            return response; 

        } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage().getBytes()) ;
        }                   
        

    }

    
}
