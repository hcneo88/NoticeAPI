package org.eservice.notice.controller;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eservice.docxapi.DocXAPI;
import org.eservice.docxapi.ImageField;
import org.eservice.docxapi.RecipientAddress;
import org.eservice.notice.component.NoticeAPI;
import org.eservice.notice.constants.Constant;
import org.eservice.notice.constants.ErrorMessage;
import org.eservice.notice.constants.NoticeNumEnum;
import org.eservice.notice.constants.NoticeQueryEnum;
import org.eservice.notice.exceptions.NoticeAPIException;
import org.eservice.notice.model.CmNoticeinstances;
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
public class GenerateNoticeAPI extends Response {


    @Autowired
    NoticeAPI noticeAPI ;

    @PostMapping("/notice/generate/userjson/pdf")
    @SuppressWarnings("unchecked") 
    public ResponseEntity<byte[]> generatePdf(@RequestBody Map<String, Object> param) {

        log.info("API version:{}", DocXAPI.apiVersionDate()) ;
        
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
            Map<String, String> imageFields = (Map<String,String>) param.get("imageFields") ; 
            Map<String, String> qrCodeFields = (Map<String, String>) param.get("qrCodeFields") ; 
            Map<String, List<Map<String, String>>> dataTables = (Map<String, List<Map<String, String>>>) param.get("dataTables") ;

            Map<String, Object> textOrImageFields = new HashMap<>();
            if (textFields != null && textFields.size() > 0) {
                for (Map.Entry<String, String> m : textFields.entrySet()) {
                    textOrImageFields.put(m.getKey(),DocXAPI.createTextField(m.getValue()));
                }
            }   
            
            if (imageFields != null && imageFields.size() > 0) {
                for (Map.Entry<String, String> m : imageFields.entrySet()) {
                    ImageField imageField = new ImageField() ;
                    imageField.setImage(m.getValue()) ; //m.getValue is base64 !
                    textOrImageFields.put(m.getKey(),imageField);
                }
            }

            if (qrCodeFields != null) { 
                if (qrCodeFields.size() > 0) {
                    for (Map.Entry<String, String> m : qrCodeFields.entrySet()) {
                        textOrImageFields.put(m.getKey(), 
                                              noticeAPI.createQrCodeField(m.getValue(),
                                              Constant.QRCODE_DEFAULTSIZE, Constant.QRCODE_DEFAULTSIZE));
                    }
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
            ByteArrayOutputStream bos = noticeAPI.generateNotice();   //If either mergeFieldErrors or tableFieldErrors are set, exception will be thrown

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

    //TODO: Include endpoint to call noticeAPI.findNoticeInstances so that a matching
    //list of instances are return for end user selection.
    @GetMapping("/notice/getinstances")
    public ResponseEntity<Map<String, Object>> getInstancesByQueryType(@RequestParam String querytype,
                                                                  @RequestParam String value) 
                                                                                        throws Exception {
        
        String startDate = LocalDate.now().minusDays(Constant.DATE_SEARCHRANGE).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ; 
        String endDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ; 
       
        log.info("Start date : {} End Date : {}", startDate, endDate) ;
        try {
            log.info("Getting notice instances by name {}", value) ;
            List <CmNoticeinstances> noticeInstances = noticeAPI.findNoticeInstances(
                                                            NoticeQueryEnum.valueOf(querytype.toUpperCase()), value, 
                                                            startDate, endDate) ;
            Gson gson = new GsonBuilder().setPrettyPrinting().create() ;
            String resultSet = gson.toJson(noticeInstances);
            
            setStatus(0);
            setMessage("");
            addItem("noticeInstances", noticeInstances) ;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/json"));
            ResponseEntity<Map<String, Object>> response = new ResponseEntity<Map<String, Object>>(getResponse(), headers, HttpStatus.OK);
            return response; 

        } catch (Exception e) {
                setStatus(-1) ;
                setMessage(e.getMessage()) ;
                return ResponseEntity.badRequest().body(getResponse()) ;
        }                   

    }

        
}
