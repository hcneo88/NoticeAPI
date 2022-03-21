package org.eservice.notice.constants;

public final class ErrorMessage {

    ErrorMessage() {} 

    //createNotice API
    public static final String E1000 =  "E1000#Unable to find notice template.";
    public static final String E1000A = "E1000A#Find more than one templates." ;
    public static final String E1001 =  "E1001#Recipient cannot be null." ;
    public static final String E1002 =  "E1002#Recipient's name cannot be blank." ;
    public static final String E1003 =  "E1003#Recipient's address line 1 and 2 cannot be blank.";
    

    //populateMergeField & populateTableFiled API
    public static final String E1004  = "E1004#Either extraneous field being passed in or merge field contains mismatched data type for notice id # " ;
    public static final String E1004A = "E1004A#Either extraneous table/table field being passed in or table column contains mismatched data type for notice id #" ;

    //loadFields
    public static final String E1006 = "E1006#No notice instance found for instance id #" ;
    
    public static final String E1007 = "E1007#Unable to find notice template #" ;

    //regenerateNotice
    public static final String E1008 = "E1008#Unable to find signatory #" ;

    //generateNotice
    public static final String E1009 = "E1009#No valid recipient address to generate notice #" ;

    public static final String E1010 = "E1010#createNotice API needs to be called first." ;

    //createImageField
    public static final String E1020 = "E1020#Image file not found at " ;

    //saveFields
    public static final String E1025 = "E1025#Unable to persit merge fields to database." ;

    public static final String E1030= "E1006A#Multiple active notice record found for notice #" ;
}
