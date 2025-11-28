package com.example.financemanager.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Configuration class for internationalization (i18n) support.
 * Enables bilingual functionality with English and Turkish languages.
 *
 * Features:
 * - Session-based locale storage
 * - URL parameter-based language switching (?lang=en or ?lang=tr)
 * - Message externalization through properties files
 * - UTF-8 encoding for proper Turkish character support
 */
@Configuration
public class LocaleConfig implements WebMvcConfigurer {

    /**
     * Configures the locale resolver to store user's language preference in the session.
     * Default locale is set to English.
     *
     * @return SessionLocaleResolver configured with English as default
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        return localeResolver;
    }

    /**
     * Configures the interceptor that detects language change requests.
     * Listens for the 'lang' parameter in requests (e.g., ?lang=tr)
     *
     * @return LocaleChangeInterceptor configured to listen for 'lang' parameter
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * Configures the message source for loading internationalized messages.
     * Messages are loaded from messages_en.properties and messages_tr.properties files.
     *
     * @return MessageSource configured with UTF-8 encoding and message properties location
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // Cache for 1 hour
        return messageSource;
    }

    /**
     * Registers the locale change interceptor with Spring MVC.
     *
     * @param registry the InterceptorRegistry to add interceptors to
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
