package org.eservice.notice.component ;



import org.eservice.notice.testsuites.CreateNoticeTestCase;
import org.eservice.notice.testsuites.GenerateExistingNoticeTestCase;
import org.eservice.notice.testsuites.GenerateNewNoticeTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

//import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApplicationStartupTask implements CommandLineRunner {
    
    @Autowired
    CreateNoticeTestCase createNoticeTestCase ;

    @Autowired
    GenerateNewNoticeTestCase setMergeFieldsTestCase;

    @Autowired
    GenerateExistingNoticeTestCase generateExistingNoticeTestCase;

    public void run(String... args) throws Exception {

        System.setProperty("user.timezone", "Asia/Singapore");
        log.info("*** Start running Self Test ***");
/*
        try {
            log.info("Unit test:createNotice API. TC3 passed. TC3A,3B,3C failed with exceptions.") ;
            createNoticeTestCase.happydayFlowType3();
            createNoticeTestCase.exceptionFlowType3A();
            createNoticeTestCase.exceptionFlowType3B();
            createNoticeTestCase.exceptionFlowType3C();
        } catch (Exception e) {
            log.error(e.getMessage()) ;
            e.printStackTrace();
        }

        try {
            log.info("Unit test:simpleFieldsHappydayFlow");
            setMergeFieldsTestCase.simpleFieldsHappydayFlow();
            log.info("Unit test completed -------------------") ;
        } catch (Exception e) {
            log.error(e.getMessage()) ;
            e.printStackTrace();
        }

        try {
            log.info("Unit test:simpleFieldsExceptionFlowA");
            setMergeFieldsTestCase.simpleFieldsExceptionFlowA();
            log.info("Unit test completed -------------------") ;
        } catch (Exception e) {
            log.error(e.getMessage()) ;
            e.printStackTrace();
        }
    
        try {
            log.info("Unit test:tableFieldsHappydayFlow");
            setMergeFieldsTestCase.tableFieldsHappydayFlow();
            log.info("Unit test completed -------------------") ;
        } catch (Exception e) {
            log.error(e.getMessage()) ;
            e.printStackTrace();
        }   

    /*    
        try {
            log.info("Unit test:generateNoticeHappydayFlow");
            generateExistingNoticeTestCase.generateNoticeHappydayFlow("2022031223315635332D");
            log.info("Unit test completed -------------------") ;
        } catch (Exception e) {
            log.error(e.getMessage()) ;
            e.printStackTrace();
        }  
    */   
        log.info("*** End running Self Test ***");
        log.info("***** Microservice application ready *****");

    }         
} 
