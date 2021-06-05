package com.sse.rcp.config;


import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@Slf4j
@ComponentScan
public class PostGreConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.druid.datasource")
    public DataSource druid() {
        return new DruidDataSource();
    }

}
