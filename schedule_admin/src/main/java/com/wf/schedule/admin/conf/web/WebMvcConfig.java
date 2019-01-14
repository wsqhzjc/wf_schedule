package com.wf.schedule.admin.conf.web;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.wf.schedule.admin.conf.interceptor.CommonInterceptor;
import com.wf.schedule.ehcache.EhcacheManager;
import net.sf.ehcache.Cache;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.jasig.cas.client.util.AssertionThreadLocalFilter;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ydh on 2017/4/6.
 */
@Configuration
@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurerAdapter implements ApplicationContextAware {
    @Value("${cas.securityUrl}")
    private String casUrl;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("default");
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public EhcacheManager ehcacheManager() {
        EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        cacheManagerFactoryBean.setConfigLocation(new ClassPathResource("conf/ehcache.xml"));
        cacheManagerFactoryBean.setShared(true);
        cacheManagerFactoryBean.afterPropertiesSet();

        EhCacheFactoryBean ehCacheFactoryBean = new EhCacheFactoryBean();
        ehCacheFactoryBean.setCacheManager(cacheManagerFactoryBean.getObject());
        ehCacheFactoryBean.setCacheName("DEFAULT_CACHE");
        ehCacheFactoryBean.afterPropertiesSet();

        EhcacheManager ehcacheManager = new EhcacheManager();
        ehcacheManager.setEhCache((Cache) ehCacheFactoryBean.getObject());
        return ehcacheManager;
    }

    @Bean
    public ServletListenerRegistrationBean servletListenerRegistrationBean() {
        ServletListenerRegistrationBean servletListenerRegistrationBean = new ServletListenerRegistrationBean();
        servletListenerRegistrationBean.setListener(new SingleSignOutHttpSessionListener());
        return servletListenerRegistrationBean;
    }

    @Bean
    @Order(1)
    public FilterRegistrationBean SingleSignOutFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        SingleSignOutFilter httpBasicFilter = new SingleSignOutFilter();
        registrationBean.setFilter(httpBasicFilter);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

//    @Bean
//    @Order(2)
//    public FilterRegistrationBean CASAuthenticationFilter() {
//        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
//        CASAuthenticationFilter httpBasicFilter = new CASAuthenticationFilter(casUrl);
//        registrationBean.setFilter(httpBasicFilter);
//        Map<String, String> initParam = new HashMap<>();
//        initParam.put("casServerLoginUrl", "http://10.33.85.211:8080/cas/login");
//        initParam.put("serverName", "http://10.33.80.12:8899");
//        registrationBean.setInitParameters(initParam);
//        registrationBean.addUrlPatterns("/*");
//        return registrationBean;
//    }
//
//    @Bean
//    @Order(3)
//    public FilterRegistrationBean Cas20ProxyReceivingTicketValidationFilter() {
//        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
//        Cas20ProxyReceivingTicketValidationFilter httpBasicFilter = new Cas20ProxyReceivingTicketValidationFilter(casUrl);
//        registrationBean.setFilter(httpBasicFilter);
//        Map<String, String> initParam = new HashMap<>();
//        initParam.put("casServerUrlPrefix", "http://10.33.85.211:8080/cas/login");
//        initParam.put("serverName", "http://10.33.80.12:8899");
//        registrationBean.setInitParameters(initParam);
//        registrationBean.addUrlPatterns("/*");
//        return registrationBean;
//    }

    @Bean
    @Order(4)
    public FilterRegistrationBean HttpServletRequestWrapperFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        HttpServletRequestWrapperFilter httpBasicFilter = new HttpServletRequestWrapperFilter();
        registrationBean.setFilter(httpBasicFilter);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @Bean
    @Order(5)
    public FilterRegistrationBean AssertionThreadLocalFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        AssertionThreadLocalFilter httpBasicFilter = new AssertionThreadLocalFilter();
        registrationBean.setFilter(httpBasicFilter);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @Bean
    public ViewResolver viewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter =
                new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.PrettyFormat
        );
        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        supportedMediaTypes.add(MediaType.TEXT_HTML);
        fastJsonHttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);
        converters.add(fastJsonHttpMessageConverter);
    }

    @Bean
    public TemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setEnableSpringELCompiler(true);
        engine.setTemplateResolver(templateResolver());
        return engine;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CommonInterceptor()).addPathPatterns("/**");
    }

    private ITemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setTemplateMode(TemplateMode.HTML);
        return resolver;
    }

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {"classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/"};

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    }
}
