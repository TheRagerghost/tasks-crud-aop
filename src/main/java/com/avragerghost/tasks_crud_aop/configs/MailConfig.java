package com.avragerghost.tasks_crud_aop.configs;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import lombok.Getter;
import lombok.Setter;

///mail-config.yml скрыт .gitignore
@ConfigurationProperties(prefix = "spring.mail")
@ConfigurationPropertiesScan
@Getter
@Setter
public class MailConfig {

    private String host;
    private int port;
    private String username;
    private String password;
    private String protocol = "smtp";
    private Map<String, String> properties = new HashMap<>();

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setProtocol(protocol);
        mailSender.setJavaMailProperties(new Properties() {
            {
                putAll(properties);
            }
        });

        return mailSender;
    }

}
