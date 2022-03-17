package org.eservice.notice.component ;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.zxing.client.j2se.MatrixToImageConfig;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.wml.JcEnumeration;
import org.eservice.docxapi.DocXAPI;
import org.eservice.docxapi.ImageField;
import org.eservice.docxapi.QrCodeField;
import org.eservice.docxapi.RecipientAddress;
import org.eservice.docxapi.TextField;
import org.eservice.notice.component.common.Util;
import org.eservice.notice.constants.Constant;
import org.eservice.notice.constants.ErrorMessage;
import org.eservice.notice.constants.NoticeNumEnum;
import org.eservice.notice.constants.NoticeQueryEnum;
import org.eservice.notice.exceptions.NoticeAPIException;
import org.eservice.notice.model.CmNoticefields;
import org.eservice.notice.model.CmNoticeinstances;
import org.eservice.notice.model.CmNotices;
import org.eservice.notice.model.CmSignatory;
import org.eservice.notice.repository.NoticeFieldsRepository;
import org.eservice.notice.repository.NoticeInstancesRepository;
import org.eservice.notice.repository.NoticeRepository;
import org.eservice.notice.repository.SignatoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/*
Sequence of API call to create new notice instances
(1)(M)  createNotice
(2)(M)  populateMergeFields - pass in null when there is no merge field
(3)     populateTableFields - when the template contains a singular table
(4)(M)  saveFields (serialize to db)

API calls to retrieve existing notices.
(5)     loadFields By InstanceId (deserialize from db) 

To generate PDF after step (4) or (5) are done
(6)     generateNotice (merge, then return PDF) 

LEGEND : (M) - MUST CALL API
*/

@Service
@Slf4j
@Data
public class NoticeAPI {

    private DocXAPI docxAPI ;

    @Autowired
    NoticeRepository noticeRepo ;

    @Autowired 
    NoticeInstancesRepository noticeInstancesRepo ;

    @Autowired
    NoticeFieldsRepository noticeFieldsRepo;

    @Autowired
    SignatoryRepository signatoryRepo ;

    @Autowired  
    Util commonService ;

    CmNoticefields noticeFields; 
    CmNotices dbNotice ;
    CmNoticeinstances dbNoticeInstances;
    CmSignatory dbSignatory; 

    RecipientAddress recipientAddress;
    
    String templatePath ;
    String letterHeadPath ;
    String docPath ;
    String signatoryPath ;

    List<String> mergeFieldErrors ;
    List<String> tableFieldErrors ;

    int mergeFieldErrorsCount ;
    int tableFieldErrorsCount ;
    //private static final Logger log = LoggerFactory.getLogger(NoticeAPI.class);


    NoticeAPI() throws Exception {
        //docxAPI = new DocXAPI() ;
        log.info("DocXAPI version:{}", DocXAPI.apiVersionDate()) ;
        templatePath = NoticeAPI.class.getResource("/templates").toURI().getPath();
        letterHeadPath = templatePath + "/letterhead"  ;
        signatoryPath = templatePath + "/signatory" ;
        docPath = NoticeAPI.class.getResource("/instances").toURI().getPath(); 

        mergeFieldErrors = new ArrayList<>();
        tableFieldErrors = new ArrayList<>();

        mergeFieldErrorsCount = 0 ;
        tableFieldErrorsCount = 0 ;
    }

    public DocXAPI getDocxAPI() {
        return this.docxAPI ;
    }

    public TextField createTextField(String text) {
        return DocXAPI.createTextField(text) ;
    }

    public ImageField createImageField(byte[] imageByte) {
         return DocXAPI.createImageField(imageByte);
    }

    public ImageField createImageField(String imagePath) throws Exception {

        byte[] imageByte = null;
        try {
            imageByte = docxAPI.loadImage(imagePath) ;
        } catch (Exception e) {
            throw new NoticeAPIException(e, ErrorMessage.E1020 + imagePath) ;
        }
        return DocXAPI.createImageField(imageByte);
   }

    //1st API to be called.
    public void createNotice(NoticeNumEnum noticeNum, RecipientAddress recipientAddress) throws Exception {
        
        List<CmNotices> dbNotices  = null;
        
        dbNotices = noticeRepo.findByNoticeNum(noticeNum.toString()) ;
        if (dbNotices == null) throw new NoticeAPIException(ErrorMessage.E1000) ;
        if (dbNotices.size() > 1) throw new NoticeAPIException(ErrorMessage.E1000A) ;
               
        dbNotice = dbNotices.get(0) ;
        if (recipientAddress == null) {
            if (dbNotice.getRecipientTypeCd() != 0 ) 
                throw new NoticeAPIException(ErrorMessage.E1001) ;
            else {
                recipientAddress = new RecipientAddress() ;
            }
        }

        //RecipientTypeCd = 0:No name,No address,  1:name, No address,  2:No name, address, 3:name, address 
        switch (dbNotice.getRecipientTypeCd()) {
            case 1 :
            case 3 : if (recipientAddress.getRecipientName().equals("")) {
                        throw new NoticeAPIException(ErrorMessage.E1002) ;
                     } 
            case 2 : Map<String, String> formattedRecipientAddress = recipientAddress.format() ;
                     String addrLine1 = formattedRecipientAddress.get("addressline1") ;
                     String addrLine2 = formattedRecipientAddress.get("addressline2") ;
                     if (addrLine1.trim().equals("") || addrLine2.trim().equals("")) 
                        throw new NoticeAPIException(ErrorMessage.E1003) ;
                     break ;
            default :
        }

        this.recipientAddress = recipientAddress ;
        String instanceId = Util.generateID() ;
        this.recipientAddress.setTransactionId(instanceId);
        this.recipientAddress.setNoticeNumber(noticeNum.toString()) ;
        this.recipientAddress.setNoticeVersion(String.valueOf(dbNotice.getRevisionNo())); 

        docxAPI = new DocXAPI();
        docxAPI.loadTemplate(templatePath + Constant.TEMPLATE_PREFIX + dbNotice.getNoticeNum() + "-" + dbNotice.getNoticeId() + ".docx");
        docxAPI.setRecipient(this.recipientAddress); //<= this docxAPI will auto insert text merge field for 3 lines address + recipient name

        log.debug("Loaded template in createNotice() API for notice number:{}", noticeNum.toString() ) ;
    }

    //VERY IMPORTANT: The first element in columnForOneRow MUST match the merge field name of the table in
    //the MSWord template !  The first column is used to "identify the table".
    public void populateTableField(String tableName, Map<String, String> columnForOneRow) throws Exception {

        if (this.dbNotice == null || this.recipientAddress == null)   
            throw new NoticeAPIException(ErrorMessage.E1010) ;

        tableFieldErrors = new ArrayList<>() ;
        tableFieldErrorsCount = 0;     
        
        List<String> fieldsDefinedInDB = new ArrayList<>();
        List<String> fieldsDefinedByUser = new ArrayList<>() ;

        for (String field : columnForOneRow.keySet()) {
            fieldsDefinedByUser.add(field) ;
        }

        String noticeId = dbNotice.getNoticeId() ;
    
        List<CmNoticefields> noticeFieldList = noticeFieldsRepo.findAllByNoticeId (noticeId) ;
        for (int i=0;i<noticeFieldList.size();i++) {
             CmNoticefields noticeField = noticeFieldList.get(i) ;
             if (noticeField.getFieldTypeCd().startsWith("T")) {  //The rest of the type A, I and Q are checked in populateMergeField
                String columnData = columnForOneRow.get(noticeField.getFieldName()) ;
                fieldsDefinedInDB.add(noticeField.getFieldName()) ;
                
                if (columnData == null) {
                    if (noticeField.getMandatoryCd().equalsIgnoreCase("Y")) {
                        tableFieldErrorsCount++ ;
                        tableFieldErrors.add(String.valueOf(tableFieldErrorsCount) + ") " + noticeField.getFieldName() + " (table column) is missing.") ;
                    } else { //create a text field with empty text so that the placeholder will get removed.
                        columnForOneRow.put(noticeField.getFieldName(), "") ;
                    }
                }  else {  //user got pass in the merge field, need to check data type
                        char dataType =  noticeField.getFieldTypeCd().charAt(1) ;
                        
                        switch (dataType) {
                            case 'S' :  break;

                            case 'N' :  if (! StringUtils.isNumeric(columnData)) {
                                           tableFieldErrorsCount++;
                                           tableFieldErrors.add(String.valueOf(tableFieldErrorsCount) + ") " + noticeField.getFieldName() + " is not defined as a numeric field.") ;
                                        }
                                        break;
                            case 'F' :  try {
                                          Float.parseFloat(columnData);
                                        } catch (NumberFormatException e){
                                            tableFieldErrorsCount++ ;
                                            tableFieldErrors.add(String.valueOf(tableFieldErrorsCount) + ") " + noticeField.getFieldName() + " is not defined as a float field.");
                                        }
                                        break;
                            case 'D' :  DateFormat sdf ;
                                        if (columnData.contains(":"))  
                                            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        else 
                                            sdf = new SimpleDateFormat("yyyy-MM-dd");
                                        sdf.setLenient(false);
                                        try {
                                            sdf.parse(columnData);
                                        } catch (Exception e) {
                                            tableFieldErrorsCount++;
                                            tableFieldErrors.add(String.valueOf(tableFieldErrorsCount) + ") " + noticeField.getFieldName() + " is not defined as a valid date field with value " + columnData);
                                        }
                                        break ;
                            default  :
                        }
                } //user got pass in the merge field
             } 
        } //For noticeFields

        fieldsDefinedByUser.removeAll(fieldsDefinedInDB) ;
        if (fieldsDefinedByUser.size() > 0) {
            for (String field : fieldsDefinedByUser) {
                tableFieldErrorsCount++ ;
                tableFieldErrors.add(String.valueOf(tableFieldErrorsCount) + ") " + field + " is not defined in database and not taken to be a valid field.");
            }
        }
        
        if (tableFieldErrors.size() == 0) {
            docxAPI.setTableMergeFields(tableName, columnForOneRow) ;
        }
       
    }     

    //Must call this API as long as there is a need to pass in reipient name and address - pass a null
    public void populateMergeFields(Map<String, Object> textOrImageFields) throws Exception {

        //if dbNotice is null => has not called createNotice API.
        if (this.dbNotice == null || this.recipientAddress == null)   
            throw new NoticeAPIException(ErrorMessage.E1010) ;

        mergeFieldErrors = new ArrayList<>() ;
        mergeFieldErrorsCount = 0;     
        Map<String, TextField> txtFields = new HashMap<String, TextField>() ;
        Map<String, ImageField> imgFields = new HashMap<String, ImageField>() ;
        
        String noticeId = dbNotice.getNoticeId() ;
        List<CmNoticefields> noticeFieldsList = noticeFieldsRepo.findAllByNoticeId (noticeId) ;

        List<String> fieldsDefinedInDB = new ArrayList<>();
        List<String> fieldsDefinedByUser = new ArrayList<>() ; 

        for (String userSuppliedField : textOrImageFields.keySet()) 
            fieldsDefinedByUser.add(userSuppliedField) ;

        //Validate that all manandatory fields in cmnoticefields are defined
        for (int i = 0 ; i < noticeFieldsList.size(); i++) {
            CmNoticefields noticeField = noticeFieldsList.get(i) ; 
            if (noticeField.getFieldTypeCd().charAt(0) == 'T') continue ;   //table, will be handled in populateTableFields
            
            Object obj = textOrImageFields.get(noticeField.getFieldName()) ;
            fieldsDefinedInDB.add(noticeField.getFieldName()) ;
            if (obj == null) {
                if (noticeField.getMandatoryCd().equalsIgnoreCase("Y")) {
                    mergeFieldErrorsCount ++ ;
                    mergeFieldErrors.add(String.valueOf(mergeFieldErrorsCount) + ") " + noticeField.getFieldName() + " is missing.") ;
                } else { //create a text field with empty text so that the placeholder will get removed when mail merging.
                    txtFields.put(noticeField.getFieldName(), DocXAPI.createTextField("")) ;
                    log.warn("Value for non mandatory merge field {} was not provided.  Set to blank by default", noticeField.getFieldName()) ;
                } 
            } else {   //Found merge field with the specifc name supplied by API user

                if (obj instanceof TextField) {
                    TextField text = (TextField) obj;
                    log.debug("Validating field {} with value {}", noticeField.getFieldName(), text.getText()) ;
                    if (Boolean.FALSE.equals(noticeField.getFieldTypeCd().startsWith("A"))) {   //A - ASCII
                        mergeFieldErrorsCount ++;
                        mergeFieldErrors.add(String.valueOf(mergeFieldErrorsCount) + ") " + noticeField.getFieldName() + " is not a simple (type 'A') merge field. It is defined as type (" + noticeField.getFieldTypeCd().charAt(0) +") " ) ;
                    } else {
                       Boolean isValid = true;
                       char dataType =  noticeField.getFieldTypeCd().charAt(1) ;
                        
                        switch (dataType) {
                            case 'S' :  break;

                            case 'N' :  if (! StringUtils.isNumeric(text.getText())) {
                                           isValid = false ;
                                           mergeFieldErrorsCount ++ ;
                                           mergeFieldErrors.add(String.valueOf(mergeFieldErrorsCount) + ") " + noticeField.getFieldName() + " is not defined as a numeric field.") ;
                                        }
                                        break;
                            case 'F' :  try {
                                          Float.parseFloat(text.getText());
                                        } catch (NumberFormatException e){
                                            isValid = false ;
                                            mergeFieldErrorsCount ++;
                                            mergeFieldErrors.add(String.valueOf(mergeFieldErrorsCount) + ") " + noticeField.getFieldName() + " is not defined as a float field.");
                                        }
                                        break;
                            case 'D' :  DateFormat sdf ;
                                        if (text.getText().contains(":"))  
                                            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        else 
                                            sdf = new SimpleDateFormat("yyyy-MM-dd");
                                        
                                        sdf.setLenient(false);
                                        try {
                                            sdf.parse(text.getText());
                                        } catch (Exception e) {
                                            isValid = false ;
                                            mergeFieldErrorsCount ++ ;
                                            mergeFieldErrors.add(String.valueOf(mergeFieldErrorsCount) + ") " + noticeField.getFieldName() + " is not a valid date field with value " + text.getText());
                                        }
                                        break ;
                            default  :
                        }
                        if (Boolean.TRUE.equals(isValid)) txtFields.put(noticeField.getFieldName(),(TextField)obj) ;
                    }
                }

                if (obj instanceof ImageField) {
                    if (Boolean.FALSE.equals(noticeField.getFieldTypeCd().startsWith("I"))) {  
                        mergeFieldErrorsCount ++; 
                        mergeFieldErrors.add(String.valueOf(mergeFieldErrorsCount) + ") " + noticeField.getFieldName() + " is not supposed to be an image field") ;
                    } else {
                        imgFields.put(noticeField.getFieldName(),(ImageField)obj) ;
                    }
                }

                //if (obj instanceof String) {   //QR code is expected
                if (obj instanceof QrCodeField) {
                    
                    if (Boolean.FALSE.equals(noticeField.getFieldTypeCd().startsWith("Q"))) {   
                        mergeFieldErrors.add(noticeField.getFieldName() + " is not an QR code.") ;
                    } else {
                        Boolean isValidQR = true;
                        QrCodeField qrCode = (QrCodeField) obj ;
                        String qrString = qrCode.getQrCodeString() ;
                        char dataType =  noticeField.getFieldTypeCd().charAt(1) ;
                        switch (dataType) {
                            case 'S' :  break;

                            case 'N' :  if (! StringUtils.isNumeric(qrString)) {
                                           isValidQR = false;
                                           mergeFieldErrorsCount++;
                                           mergeFieldErrors.add(String.valueOf(mergeFieldErrorsCount) + ") " + noticeField.getFieldName() + " is not a numeric field with QR value " + qrString) ;
                                        }
                                        break;
                            case 'F' :  try {
                                          Float.parseFloat(qrString);
                                        } catch (NumberFormatException e){
                                            isValidQR = false;
                                            mergeFieldErrorsCount ++;
                                            mergeFieldErrors.add(String.valueOf(mergeFieldErrorsCount) + ") " + noticeField.getFieldName() + " is not a float field with QR value " + qrString);
                                        }
                                        break;
                            case 'D' :  DateFormat sdf ;
                                        if (qrString.contains(":"))  
                                            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        else 
                                            sdf = new SimpleDateFormat("yyyy-MM-dd");
                                        sdf.setLenient(false);
                                        try {
                                            sdf.parse(qrString);
                                        } catch (Exception e) {
                                            isValidQR = false ;
                                            mergeFieldErrorsCount ++ ;
                                            mergeFieldErrors.add(String.valueOf(mergeFieldErrorsCount) + ") " + noticeField.getFieldName() + " is not a valid date field with QR value " + qrString);
                                        }
                                        break ;
                            default  :
                        }
                        if (Boolean.TRUE.equals(isValidQR)) {
                            //byte[] qrImage = DocXAPI.createQRCode(qrString, 50, 50) ;
                            MatrixToImageConfig matrixConfig = new MatrixToImageConfig(DocXAPI.Colors.WHITE.getArgb(), DocXAPI.Colors.ORANGE.getArgb());
                            imgFields.put(noticeField.getFieldName(), DocXAPI.createImageField(qrCode.generateQrCode(matrixConfig)));
                        }
                    } 
                } //QR code

                
            
            }  //Found merge field supplied by API user
        } // For merge field defined in NoticeFields 

        fieldsDefinedByUser.removeAll(fieldsDefinedInDB) ;
        if (fieldsDefinedByUser.size() > 0) {
            for (String field : fieldsDefinedByUser) {
                mergeFieldErrorsCount++ ;
                mergeFieldErrors.add(String.valueOf(mergeFieldErrorsCount) + ") " + field + " is not defined in database and not taken to be a valid field.") ;
            }
        }

        if (mergeFieldErrors.size() == 0) {
            if (txtFields.size() > 0) docxAPI.setTextMergeFields(txtFields)  ;
            if (imgFields.size() > 0) docxAPI.setImageMergeFields(imgFields) ;
        }
        
    }

    public void dbUploadFields() throws Exception {

        String mergeFields = docxAPI.serializeMergeFields() ;
        String headerFooterFields = docxAPI.serializeHeaderFooter() ; // <= WILL BE BLANK when the header/footer is from letterhead  
        log.debug("Merge fields to be saved to database :{}", mergeFields) ;

        dbNoticeInstances = new CmNoticeinstances() ;
        try {

            Date instanceDate = new SimpleDateFormat("yyyy-MM-dd").parse(this.recipientAddress.getTransactionDate()); 
            System.out.println("DATE:" + instanceDate) ;
            Timestamp instanceTimestamp = new Timestamp(instanceDate.getTime()) ;
            
            dbNoticeInstances.setInstanceId(this.getRecipientAddress().getTransactionId()) ;   //Auto created during createNotice API calls
            dbNoticeInstances.setInstanceDate (instanceTimestamp); 
            dbNoticeInstances.setAddrTypeCd(String.valueOf(this.recipientAddress.getAddressType()));
            dbNoticeInstances.setBldgName(this.recipientAddress.getBuildingName());
            dbNoticeInstances.setBlkHseNum(this.recipientAddress.getBlkHseNo());
            dbNoticeInstances.setStreetname(this.recipientAddress.getStreetName());
            dbNoticeInstances.setFloorNum(this.recipientAddress.getFloorNo());
            dbNoticeInstances.setPostalCd(this.recipientAddress.getPostalCd());
            dbNoticeInstances.setRecipientName(this.recipientAddress.getRecipientName());
            dbNoticeInstances.setHeaderfooterFields(headerFooterFields);
            dbNoticeInstances.setMergeFields(mergeFields);
            dbNoticeInstances.setNoticeId(dbNotice.getNoticeId());
            dbNoticeInstances.setNoticeNum(dbNotice.getNoticeNum());
            dbNoticeInstances.setPrintTypeCd(dbNotice.getPrintTypeCd());
            dbNoticeInstances.setOutsourceStatusCd("");
            dbNoticeInstances.setCreatedBy("SYSTEM");
            dbNoticeInstances.setUpdatedBy("SYSTEM");
            dbNoticeInstances.setVersionNo(new Random().nextInt(99999));

            dbNoticeInstances.setSignatoryId(dbNotice.getSignatoryId());
            dbNoticeInstances.setLetterheadId(dbNotice.getLetterheadId());

            if (dbNotice.getPrintTypeCd().equalsIgnoreCase("B") ||
                                                    dbNotice.getPrintTypeCd().equalsIgnoreCase("O") ) 
                dbNoticeInstances.setOutsourceActionCd("S") ;
            else 
                dbNoticeInstances.setOutsourceActionCd("");

            Timestamp now = new Timestamp(new Date().getTime()) ;
            dbNoticeInstances.setCreatedDate(now);
            dbNoticeInstances.setUpdatedDate(now); 

            noticeInstancesRepo.save(dbNoticeInstances) ;
        
        } catch (Exception e) {
            throw new NoticeAPIException(e, ErrorMessage.E1025) ;
        }

        
    }

    public List<CmNoticeinstances> findNoticeInstances(NoticeQueryEnum queryField, String queryValue, Date startDate, Date endDate ) throws Exception {

        List<CmNoticeinstances> instances = null ;
        switch (queryField) {
            case NAME       :   instances = noticeInstancesRepo.findByRecipientName(queryValue, startDate, endDate, Constant.RETURN_ROW_LIMIT);
                                break ;
            case NOTICEID   :   instances = noticeInstancesRepo.findByNoticeId(queryValue, startDate, endDate, Constant.RETURN_ROW_LIMIT);
                                break ;

            case NOTICENUM  :   instances = noticeInstancesRepo.findByNoticeNum(queryValue, startDate, endDate, Constant.RETURN_ROW_LIMIT);
                                break ;
        } 
        
        return instances ;
    }

    public ByteArrayOutputStream generateByInstanceId(String instanceId) throws Exception {
         
        dbNoticeInstances = noticeInstancesRepo.findByInstanceId(instanceId) ;
        if (dbNoticeInstances == null) {
            throw new NoticeAPIException(ErrorMessage.E1006 + instanceId ) ;
        }
        //String noticeId = dbNoticeInstances.getNoticeId() ;
        return regenerateNotice();
    }

    public ByteArrayOutputStream regenerateNotice() throws Exception {
        String jsonFields = dbNoticeInstances.getMergeFields();
        String jsonHeaderFooterFields = dbNoticeInstances.getHeaderfooterFields() ; 
        
        docxAPI = new DocXAPI();
        docxAPI.deserializeMergeFields(jsonFields);
        docxAPI.deserializeHeaderFooters(jsonHeaderFooterFields);  //<= only got data if header/footer was created programmatically 
        docxAPI.loadTemplate(templatePath + Constant.TEMPLATE_PREFIX + dbNoticeInstances.getNoticeNum() + "-" + dbNoticeInstances.getNoticeId() + ".docx");

        String letterHeadId = dbNoticeInstances.getLetterheadId() ;
        if (Boolean.FALSE.equals(letterHeadId.equalsIgnoreCase("Z"))) {
            String letterHeadFilename =  letterHeadPath + Constant.LETTERHEAD_PREFIX + letterHeadId + ".docx" ;
            docxAPI.mergeHeaderFooter(letterHeadFilename);
        }

        String signatoryId = dbNoticeInstances.getSignatoryId() ;
        if (Boolean.FALSE.equals(signatoryId.equalsIgnoreCase("Z"))) {
            
            dbSignatory = signatoryRepo.findBySignatoryId(signatoryId) ;
            if (dbSignatory == null) {
                throw new NoticeAPIException(ErrorMessage.E1008 + signatoryId) ;
            }

            String signatoryFilename = signatoryPath + Constant.SIGNATORY_PREFIX + signatoryId + ".jpg" ;
            byte[] signatureImage = docxAPI.loadImage(signatoryFilename) ;
            List<String> designations = new ArrayList<>();
            if (Boolean.FALSE.equals(dbSignatory.getDesignationLine1().equalsIgnoreCase(""))) {
                designations.add(dbSignatory.getDesignationLine1()) ;
            }
            if (Boolean.FALSE.equals(dbSignatory.getDesignationLine2().equalsIgnoreCase(""))) {
                designations.add(dbSignatory.getDesignationLine2()) ;
            }
            if (Boolean.FALSE.equals(dbSignatory.getDesignationLine3().equalsIgnoreCase(""))) {
                designations.add(dbSignatory.getDesignationLine3()) ;
            }
            docxAPI.createSimpleSignOffParagraph(dbSignatory.getSignoffText(), signatureImage, designations , JcEnumeration.LEFT);
        }

        if (Boolean.FALSE.equals(docxAPI.nothingToMerge())) {
                docxAPI.mailMerge();
        }  
        
        Date dateNow = new Date();
        SimpleDateFormat dft = new SimpleDateFormat("yyyyMMddHHmmss");
        String now = dft.format(dateNow); 
        String docFilename =   docPath + Constant.DOCUMENT_PREFIX + dbNoticeInstances.getNoticeNum() + "-" + 
                                                  dbNoticeInstances.getInstanceId() + 
                                                  "-copy (" + now + ").docx" ;

        docxAPI.saveDoc(docFilename);
        ByteArrayOutputStream bOS = docxAPI.saveAsPDF(docFilename) ;
        log.info("{} successfully re-generated.", docFilename) ;
        return bOS ;
    }

    public void allFieldsReady() throws Exception {

        String errorMessage = "" ;

        if (mergeFieldErrors.size() > 0) {
            String errMsg = mergeFieldErrors.toString().replace("[", "").replace("]", "").replaceAll(",", "\r\n") ;
            errorMessage = ErrorMessage.E1004 + dbNotice.getNoticeNum() + ":\r\n " + errMsg + "\r\n\r\n";
        }

        if (tableFieldErrors.size() > 0 ) {
            String errMsg = tableFieldErrors.toString().replace("[", "").replace("]", "").replaceAll(",", "\r\n") ;
            errorMessage = errorMessage.concat(ErrorMessage.E1004A + dbNotice.getNoticeNum() + ":\r\n " + errMsg); 
        }

        if (mergeFieldErrors.size() > 0 || tableFieldErrors.size() > 0) {
            mergeFieldErrors = new ArrayList<>();
            mergeFieldErrorsCount = 0 ;
            tableFieldErrors = new ArrayList<>();
            tableFieldErrorsCount = 0;
            throw new NoticeAPIException(errorMessage) ;
        }
        
    }

    public ByteArrayOutputStream generateNotice() throws Exception {

          
        String letterHeadId = dbNotice.getLetterheadId() ;
       
        if (Boolean.FALSE.equals(letterHeadId.equalsIgnoreCase("Z"))) {
            String letterHeadFilename =  letterHeadPath + Constant.LETTERHEAD_PREFIX + letterHeadId + ".docx" ;
            docxAPI.mergeHeaderFooter(letterHeadFilename);
        }

        String signatoryId = dbNotice.getSignatoryId() ;
        dbSignatory = signatoryRepo.findBySignatoryId(signatoryId) ;
        if (dbSignatory == null) {
            throw new NoticeAPIException(ErrorMessage.E1008 + signatoryId) ;
        }

        String signatoryFilename = signatoryPath + Constant.SIGNATORY_PREFIX + signatoryId + ".jpg" ;
        byte[] signatureImage = docxAPI.loadImage(signatoryFilename) ;
        List<String> designations = new ArrayList<>();
        if (Boolean.FALSE.equals(dbSignatory.getDesignationLine1().equalsIgnoreCase(""))) {
            designations.add(dbSignatory.getDesignationLine1()) ;
        }
        if (Boolean.FALSE.equals(dbSignatory.getDesignationLine2().equalsIgnoreCase(""))) {
            designations.add(dbSignatory.getDesignationLine2()) ;
        }
        if (Boolean.FALSE.equals(dbSignatory.getDesignationLine3().equalsIgnoreCase(""))) {
            designations.add(dbSignatory.getDesignationLine3()) ;
        }
        docxAPI.createSimpleSignOffParagraph(dbSignatory.getSignoffText(), signatureImage, designations , JcEnumeration.LEFT);
        docxAPI.mailMerge();
        String docFilename =   docPath + Constant.DOCUMENT_PREFIX + dbNoticeInstances.getNoticeNum() + "-" + 
                                         dbNoticeInstances.getInstanceId() + ".docx" ;
   
        docxAPI.saveDoc(docFilename);
        ByteArrayOutputStream bOS = docxAPI.saveAsPDF(docFilename) ;
        log.info("{} successfully generated.", docFilename) ;
        return bOS ;
    }

}
// 