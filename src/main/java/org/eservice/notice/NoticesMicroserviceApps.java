package org.eservice.notice;


import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(exclude = {SessionAutoConfiguration.class})
@EnableWebMvc 
@ComponentScan(basePackages={"org.eservice.notice.**"})
@EntityScan(basePackages={"org.eservice.notice.model"})
@EnableConfigurationProperties
@Slf4j
public class NoticesMicroserviceApps implements CommandLineRunner {

	@Autowired 
	private ApplicationContext applicationContext;  //https://roytuts.com/spring-conditionalonexpression-example/


	public static void main(String[] args) {
		
		SpringApplication.run(NoticesMicroserviceApps.class, args);	

	}

	@Override
	public void run(String... args) throws Exception {
		String[] beans = applicationContext.getBeanDefinitionNames();
		Arrays.sort(beans);
		// Could be useful in future :
		// boolean isLoaded = Arrays.stream(beans).anyMatch("module"::equalsIgnoreCase);
		for (int i=0;i<beans.length;i++)
			log.debug("Loaded beans:" + beans[i]);
	}

}
