package io.mosip.kernel.emailnotification.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class CommonConfig {

    /**
     * Bean to register RequestResponse Filter.
     *
     * @return reqResFilter.
     */
    @Bean
    public FilterRegistrationBean<Filter> registerReqResFilter() {
        FilterRegistrationBean<Filter> reqResFilter = new FilterRegistrationBean<>();
        reqResFilter.setFilter(getReqResFilter());
        reqResFilter.setOrder(1);
        return reqResFilter;
    }
    /**
     * Bean for RequestResponseFilter.
     *
     * @return reqResFilter object.
     */
    @Bean
    public Filter getReqResFilter() {
        return new ReqResFilter();
    }
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new AfterburnerModule());
        // .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) if needed
    }
}