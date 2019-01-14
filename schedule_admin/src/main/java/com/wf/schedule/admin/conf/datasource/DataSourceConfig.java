package com.wf.schedule.admin.conf.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by pdl on 2017/6/27.
 */
@Configuration
public class DataSourceConfig {
    @Autowired
    private MysqlDataSourceProperties mysqlDataSourceWriteProperties;

    //主数据源mysql
    @Bean(name = "dataSource")
    @Primary
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(mysqlDataSourceWriteProperties.getDriverClassName());
        dataSource.setUrl(mysqlDataSourceWriteProperties.getUrl());
        dataSource.setUsername(mysqlDataSourceWriteProperties.getUsername());
        dataSource.setPassword(mysqlDataSourceWriteProperties.getPassword());
        dataSource.setValidationQuery("select 1");
        try {
            dataSource.init();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataSource;
    }

    // ==========mybatis============
    @Bean("sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setMapperLocations(applicationContext.getResources("classpath*:mapper/*.xml"));
        sessionFactory.setTypeAliasesPackage("com.wf.schedule.admin.po");
        return sessionFactory.getObject();
    }

    /**
     * 配置事务管理器
     */
    @Bean
    public DataSourceTransactionManager transactionManager() throws Exception {
        return new DataSourceTransactionManager(dataSource());
    }

}
