package org.eservice.notice.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javafaker.Faker;

import org.apache.commons.io.FileUtils;
import org.eservice.docxapi.ImageField;
import org.eservice.docxapi.RecipientAddress;
import org.eservice.notice.component.NoticeAPI;
import org.eservice.notice.component.common.Util;
import org.eservice.notice.constants.Constant;
import org.eservice.notice.constants.ErrorMessage;
import org.eservice.notice.constants.NoticeNumEnum;
import org.eservice.notice.exceptions.NoticeAPIException;
import org.eservice.notice.model.CmNoticefields;
import org.eservice.notice.model.CmNotices;
import org.eservice.notice.model.http.Response;
import org.eservice.notice.repository.NoticeFieldsRepository;
import org.eservice.notice.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
//@ProfilerAnnotation
public class GenerateProgramCode extends Response {

    @Autowired 
    NoticeRepository noticeRepo;

    @Autowired
    NoticeFieldsRepository noticeFieldRepo ;

    @Autowired 
    NoticeAPI noticeAPI;

    @Value( "${GenerateProgramCode.singlemockfile}" )
    private Boolean singleFile;

    @GetMapping("/code/generate/mergefields")
    public ResponseEntity<String> generateMergeFieldTemplate() throws Exception  {

        List<CmNotices> noticesDAO =  (List<CmNotices>) noticeRepo.findAll() ;
        
        String segregatedProgramCode = "" ; 
        String consolidatedProgramCode = "" ;
        String apiProgramCode = "" ;
        Faker dataFaker;

        if (noticesDAO != null) {
            
            dataFaker = new Faker();
            int iteration = 0 ;
            for (CmNotices noticeDAO : noticesDAO) {
                String strCode = "";
                String tableCode = ""; 
                iteration++ ;

                //Get all the merge fields in CM_NoticeFields table
                String noticeId = noticeDAO.getNoticeId() ;
                //List<CmNoticefields> fields = null; 
                long recordCount = noticeFieldRepo.countByNoticeId(noticeId);
                List<CmNoticefields> fields = noticeFieldRepo.findAllByNoticeId(noticeId) ;

                Boolean hasTextOrImageField = false;
                Boolean hasTableField = false ;
                strCode = String.join(System.getProperty("line.separator"),
                                    "   @GetMapping(\"/notice/generate/static/mockpdf/" + noticeDAO.getNoticeNum() + "\")",
                                    "   public ResponseEntity<byte[]> generateTemplate" + noticeDAO.getNoticeNum() + "() throws Exception {",
                                    "       RecipientAddress recipientAddress = getMockRecipient() ;",
                                    "       try {",
                                    "           noticeAPI.createNotice(NoticeNumEnum.valueOf(\"" + noticeDAO.getNoticeNum() + 
                                    "\"), getMockRecipient());", 
                                    "",
                                    generateImage(noticeAPI.getImagePath())
                                ) ;
                
                if (recordCount > 0) {
                    
                    strCode = strCode.concat(System.getProperty("line.separator") +
                        "          Map<String, Object> textOrImageMergeFields = new HashMap<>() ;"                    
                    );  

                    for (CmNoticefields fld : fields) {

                        String fldType = fld.getFieldTypeCd() ;
                        if (fldType.charAt(0) == 'T') {
                            hasTableField = true;
                            tableCode = "";
                            continue ;
                        }

                        hasTextOrImageField = true;
                        if (fldType.charAt(0) == 'I') {
                            strCode = strCode.concat(System.getProperty("line.separator") +
                                            "          textOrImageMergeFields.put(\"" + fld.getFieldName() +
                                            "\",DocXAPI.createImageField(imageField.getImage())) ;") ;
                        } else {  //type A or Q
                            String fieldValue = dataFaker.book().title() ;
                            if (fldType.charAt(1) == 'N') fieldValue = String.valueOf(dataFaker.number().numberBetween(1000, 12000)) ; //String.valueOf(Util.rnd.nextInt(1000)) ;
                            if (fldType.charAt(1) == 'F') fieldValue = String.valueOf(Util.rnd.nextInt(5000)).concat(".").concat(String.valueOf(Util.rnd.nextInt(99)));
                            if (fldType.charAt(1) == 'D') fieldValue =  Util.generateRandomDateString().concat(" 12:11:10") ;
                            if (fldType.charAt(0) == 'Q') {
                                if (fieldValue.length() > 25) fieldValue = fieldValue.substring(0, 24) ;
                                strCode = strCode.concat(System.getProperty("line.separator") +
                                                        "          textOrImageMergeFields.put(\"" + fld.getFieldName() +
                                                        "\",DocXAPI.createQrCodeField(\""  + fieldValue                +
                                                        "\", Constant.QRCODE_DEFAULTSIZE, Constant.QRCODE_DEFAULTSIZE)) ;");
                            } else {  //Type A = ASCII
                                    strCode = strCode.concat(System.getProperty("line.separator") +
                                                        "          textOrImageMergeFields.put(\"" + fld.getFieldName() +
                                                        "\",DocXAPI.createTextField(\""  + fieldValue + "\")) ;");
                            } 
                        }   
                    }

                    if (Boolean.TRUE.equals(hasTextOrImageField)) {  
                        strCode = strCode.concat(System.getProperty("line.separator") + 
                                             "          noticeAPI.populateMergeFields(textOrImageMergeFields) ;" +
                                             System.getProperty("line.separator")); 
                    }
                    //handle table
    
                    if (Boolean.TRUE.equals(hasTableField)) {
                        tableCode = String.join(System.getProperty("line.separator"),
                                                    "",
                                                    "           Map<String, String> columnsForOneRow = new HashMap<>() ;",
                                                    "" );
                        for (int tr=0;tr<Util.rnd.nextInt(10) + 1 ;tr++) {
    
                            if (tr>0) 
                                tableCode = tableCode.concat(System.getProperty("line.separator") +
                                                "           columnsForOneRow = new HashMap<>() ;") ;
    
                            for (CmNoticefields fld: fields) {
                                String fldType = fld.getFieldTypeCd() ;
                                if (fldType.charAt(0) == 'T') {
    
                                        String fieldValue = dataFaker.programmingLanguage().name();   //esports().event() ;
                                        if (fldType.charAt(1) == 'N') fieldValue = String.valueOf(Util.rnd.nextInt(1000)) ;
                                        if (fldType.charAt(1) == 'F') fieldValue = String.valueOf(Util.rnd.nextInt(5000)).concat(".").concat(String.valueOf(Util.rnd.nextInt(99)));
                                        if (fldType.charAt(1) == 'D') fieldValue = Util.generateRandomDateString().concat(" 12:11:10") ;
                                
                                        tableCode = tableCode.concat(System.getProperty("line.separator") +
                                                    "           columnsForOneRow.put(\""  + fld.getFieldName() + "\",\"" + fieldValue + "\");") ;
                                }
                            } //for table fields
                     //       if (Boolean.TRUE.equals(hasTableField))
                            tableCode = tableCode.concat(System.getProperty("line.separator") + 
                                                "           noticeAPI.populateTableField(\"Table-" + noticeDAO.getNoticeNum() + 
                                                "\", columnsForOneRow);" + 
                                                System.getProperty("line.separator")) ;
                        } //iterate thru random no of records for table.
                    } //has table fields
                
                
                } //Record count in NoticeField > 0    
        
                segregatedProgramCode = segregatedProgramCode.concat(generatClassStubBegin(noticeDAO.getNoticeNum())) ; 
                segregatedProgramCode = segregatedProgramCode.concat(generateRecipientAddress()) ;
                if (strCode.length() > 0 || tableCode.length() > 0) {
                    segregatedProgramCode = segregatedProgramCode.concat(strCode).concat(tableCode) ;
                    segregatedProgramCode = segregatedProgramCode.concat(System.getProperty("line.separator"));
                }

                segregatedProgramCode = segregatedProgramCode.concat(System.getProperty("line.separator") + 
                                                    "          noticeAPI.allFieldsReady();" + 
                                                    System.getProperty("line.separator") +
                                                    "          noticeAPI.dbUploadFields();" +
                                                    System.getProperty("line.separator") +
                                                    "          ByteArrayOutputStream bos = noticeAPI.generateNotice();" + 
                                                    System.getProperty("line.separator") +
                                                   // "   }"                               +                                                    
                                                    System.getProperty("line.separator")
                                                 ) ;

                segregatedProgramCode = segregatedProgramCode.concat(
                                                generateClassStubEnd(noticeDAO.getNoticeNum())            +
                                                "}"                                                       +
                                                System.getProperty("line.separator")                      +
                                                System.getProperty("line.separator")                      +
                                                "// ----------------------------------------------------" +
                                                 System.getProperty("line.separator")                     +
                                                 System.getProperty("line.separator")
                                                );
                
                                               
                if (Boolean.FALSE.equals(singleFile)) {
                    String filePath = noticeAPI.getGeneratedProgramPath() + "/" + noticeDAO.getNoticeNum() + ".java" ;
                    FileUtils.writeStringToFile(new File(filePath),segregatedProgramCode, StandardCharsets.UTF_8);
                    consolidatedProgramCode = consolidatedProgramCode.concat(segregatedProgramCode) ;
                }
                segregatedProgramCode = "" ;

                
                if (iteration == 1) {
                    apiProgramCode = apiProgramCode.concat(generatClassStubBegin("GenerateUsingMocks")) ;
                    apiProgramCode = apiProgramCode.concat(generateRecipientAddress()) ;
                }
                apiProgramCode = apiProgramCode.concat(strCode).concat(tableCode) ;
                apiProgramCode = apiProgramCode.concat(System.getProperty("line.separator"));

                apiProgramCode = apiProgramCode.concat(System.getProperty("line.separator") + 
                                                    "          noticeAPI.allFieldsReady();" + 
                                                    System.getProperty("line.separator") +
                                                    "          noticeAPI.dbUploadFields();" +
                                                    System.getProperty("line.separator") +
                                                    "          ByteArrayOutputStream bos = noticeAPI.generateNotice();" + 
                                                    System.getProperty("line.separator") +
                                                   // "   }"                               +       f/code                                             
                                                    System.getProperty("line.separator")
                                                 ) ;
                                                    
                apiProgramCode = apiProgramCode.concat(
                    generateClassStubEnd("GenerateUsingMocks")                + 
                    System.getProperty("line.separator")                      +
                    System.getProperty("line.separator")                      +
                    "// ----------------------------------------------------" +
                    System.getProperty("line.separator")                      +
                    System.getProperty("line.separator")
            );
            
            }  //Get each Notice

            apiProgramCode = apiProgramCode.concat("}") ;
            if (Boolean.TRUE.equals(singleFile)) {
                String filePath = noticeAPI.getGeneratedProgramPath() + "/" + "GenerateUsingMocks.java" ;
                FileUtils.writeStringToFile(new File(filePath),apiProgramCode, StandardCharsets.UTF_8);
                consolidatedProgramCode = consolidatedProgramCode.concat(apiProgramCode) ;
            }

        } // if NoticeDAO not null

        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/plain"));
        return new ResponseEntity<String>(consolidatedProgramCode,headers,HttpStatus.OK) ;

    }
    
    private String generatClassStubBegin(String className) {

        String classStub = String.join(System.getProperty("line.separator"),
                "package org.eservice.notice.controller.mockup;",
                "",
                "import java.util.HashMap;",
                "import java.util.Map;",
                "import org.eservice.docxapi.DocXAPI;",
                "import org.eservice.docxapi.ImageField;",
                "import org.eservice.docxapi.RecipientAddress;",
                "import org.eservice.notice.component.NoticeAPI;",
                "import org.eservice.notice.constants.NoticeNumEnum;",
                "import org.eservice.notice.model.http.Response;",
                "import org.springframework.beans.factory.annotation.Autowired;",
                "import org.springframework.http.ContentDisposition;",
                "import org.springframework.http.HttpHeaders;",
                "import org.springframework.http.HttpStatus;",
                "import org.springframework.http.MediaType;",
                "import org.springframework.http.ResponseEntity;",
                "import org.springframework.stereotype.Component;",
                "import org.springframework.web.bind.annotation.GetMapping;",
                "import org.springframework.web.bind.annotation.RestController;",
                "import lombok.extern.slf4j.Slf4j;",
                "import java.io.ByteArrayOutputStream;",
                "import java.util.Date;",
                "import java.text.DateFormat;",
                "import java.text.SimpleDateFormat;",
                "import org.eservice.notice.constants.Constant;",
                "",
                "@Slf4j",
                "@RestController",
                "public class " + className + " extends Response {",
                "",
                "   @Autowired",
                "   NoticeAPI noticeAPI;",
                "",
                ""
        );

        return classStub ;
    }
     
    private String generateClassStubEnd(String className) {

        String classStub = String.join(System.getProperty("line.separator"),
                "           HttpHeaders headers = new HttpHeaders();",
                "           ContentDisposition contentDisposition =", 
                "                   ContentDisposition.builder(\"inline\").filename(\"" + 
                                                    className + "\" + \"-\" + " + "recipientAddress.getTransactionId()",  
                "                                                      + \".pdf\").build();",
                "",
                "",
                "           headers.setContentType(MediaType.parseMediaType(\"application/pdf\"));",
                "           headers.setContentDisposition(contentDisposition);",
                "           headers.setCacheControl(\"must-revalidate, post-check=0, pre-check=0\");",
                "           ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(bos.toByteArray(), headers, HttpStatus.OK);",
                "           return response;",
                "   } catch (Exception e) {",
                "           log.error(e.getMessage());",
                "           e.printStackTrace();",    
                "           return ResponseEntity.badRequest().body(e.getMessage().getBytes()) ;",                        
                "   }",
            "    }"
        ) ;
        return classStub ;

    }

    private String generateRecipientAddress() { 

        Faker dataFaker = new Faker();

        String recipientAddress = String.join(
                System.getProperty("line.separator"),
                "   public RecipientAddress getMockRecipient() {",
                "       RecipientAddress recipientAddress = new RecipientAddress();" ,
                "",
                "       recipientAddress.setRecipientName(\"" + dataFaker.name().firstName() + "\");",
                "       recipientAddress.setAddressType('S');" ,
                "       recipientAddress.setBlkHseNo(\"" + dataFaker.number().numberBetween(100, 800)+ "\");" ,
                "       recipientAddress.setFloorNo(\"" +  dataFaker.number().numberBetween(1, 99) + "\");",
                "       recipientAddress.setUnitNo(\"" +   dataFaker.number().numberBetween(100,2000) + "\");",  
                "       recipientAddress.setStreetName(\"" + dataFaker.address().streetName() + "\");",
                "       recipientAddress.setPostalCd(\"" +  dataFaker.number().numberBetween(100000, 200000) +  "\");",
                "",
                "       DateFormat dateFormat = new SimpleDateFormat(\"yyyy-MM-dd\");",  
                "       String strDate = dateFormat.format(new Date());",      
                "       recipientAddress.setTransactionDate(strDate);",
                "       return recipientAddress;",
                "",
                "   }",
                "",
                ""
        ) ;
        return recipientAddress ;
    }

    public String generateImage(String imagePath) throws Exception {

        String imageString = String.join (System.getProperty("line.separator"),
            "           ImageField imageField = noticeAPI.createImageField(\"" + imagePath + "/Emoji.jpg\") ;",
            "//         ***** Start *****");

        return imageString ;
    }


    @GetMapping("/notice/generate/dynamic/mockpdf/{noticeNumber}")
    public ResponseEntity<byte[]> generateMockPdf(@PathVariable String noticeNumber) throws Exception  {

        Faker dataFaker = new Faker();
        ImageField mockImageField = noticeAPI.createImageField(noticeAPI.getImagePath() + "/Emoji.jpg");

        Map <String, Object> textOrImageFields ;  
        Map <String, String> columnForOneRow ;

        try {

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");  
            Calendar.getInstance().getTime();
           // String strDate = dateFormat.format(new Date());
            String strDate = dateFormat.format(Calendar.getInstance().getTime()) ; 

            RecipientAddress recipientAddress = new RecipientAddress();
            recipientAddress.setRecipientName(dataFaker.name().firstName());
            recipientAddress.setAddressType('S');
            recipientAddress.setBlkHseNo(String.valueOf(dataFaker.number().numberBetween(100, 800)));
            recipientAddress.setFloorNo(String.valueOf(dataFaker.number().numberBetween(1, 99)));
            recipientAddress.setUnitNo(String.valueOf(dataFaker.number().numberBetween(100,5000)));
            recipientAddress.setStreetName(dataFaker.address().streetName());
            recipientAddress.setPostalCd(String.valueOf(dataFaker.number().numberBetween(100000, 200000)));
            recipientAddress.setTransactionDate(strDate);

            noticeAPI.createNotice(NoticeNumEnum.valueOf(noticeNumber), recipientAddress);
            
            List<CmNotices> noticesDAO =  noticeRepo.findByNoticeNum(noticeNumber) ;
            if (noticesDAO != null) {

                if (noticesDAO.size() > 1) {
                    throw new NoticeAPIException(ErrorMessage.E1030) ;
                }

                CmNotices noticeDAO = noticesDAO.get(0) ;
                List<CmNoticefields> mergeFields = noticeFieldRepo.findMergeFieldsByNoticeId(noticeDAO.getNoticeId()) ;           
                if (mergeFields != null && mergeFields.size() > 0) {
                    
                    textOrImageFields = new HashMap<>();
                    for (CmNoticefields fld : mergeFields) {
                            if (fld.getFieldTypeCd().charAt(0) == 'I') {
                                textOrImageFields.put(fld.getFieldName(), mockImageField);
                            } else {
                                String fieldValue = Util.generateFakerData(fld.getFieldTypeCd().charAt(1)) ;
                                if (fld.getFieldTypeCd().charAt(0) == 'A')    
                                    textOrImageFields.put(fld.getFieldName(),
                                                         noticeAPI.createTextField(fieldValue)) ;
                                else // MUST be a Q - qr type
                                    textOrImageFields.put(fld.getFieldName(),
                                                         noticeAPI.createQrCodeField(fieldValue,
                                                         Constant.QRCODE_DEFAULTSIZE, 
                                                         Constant.QRCODE_DEFAULTSIZE)) ;
                            }  //Else for field type code = A, Q
                    } // MergeField loop
                    noticeAPI.populateMergeFields(textOrImageFields);
                } //Got merge field


                //Do for table fields
                List<CmNoticefields> tableFields = noticeFieldRepo.findTableFieldsByNoticeId(noticeDAO.getNoticeId()) ;           
                if (tableFields != null && tableFields.size() > 0) {
                    
                        for (int i=0;i<5;i++) {
                            columnForOneRow = new HashMap<>();
                            for (CmNoticefields fld : tableFields) {
                                String fieldValue = Util.generateFakerData(fld.getFieldTypeCd().charAt(1)) ;
                                columnForOneRow.put(fld.getFieldName(), fieldValue) ;   
                            }
                            noticeAPI.populateTableField("TableA", columnForOneRow);
                        }
                } // got table fields
            }
            
            noticeAPI.allFieldsReady();
            noticeAPI.dbUploadFields();
            ByteArrayOutputStream bos = noticeAPI.generateNotice();
  
            HttpHeaders headers = new HttpHeaders();
            ContentDisposition contentDisposition =
                     ContentDisposition.builder("inline").filename("GenerateUsingMocks" + "-" + recipientAddress.getTransactionId()
                                                        + ".pdf").build();
  
            headers.setContentType(MediaType.parseMediaType("application/pdf"));
            headers.setContentDisposition(contentDisposition);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(bos.toByteArray(), headers, HttpStatus.OK);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage().getBytes()) ;
        }
          
    } //Method

}  //END OF CLASS

