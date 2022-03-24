package org.eservice.notice;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication(exclude = {SessionAutoConfiguration.class})
@EnableWebMvc 
@ComponentScan(basePackages={"org.eservice.notice.**"})
@EntityScan(basePackages={"org.eservice.notice.model"})
public class NoticesMicroserviceApps {

	
	
	public static void main(String[] args) {
		SpringApplication.run(NoticesMicroserviceApps.class, args);	
	}

}
