package net.thumbtack.forums.configuration;

import net.thumbtack.forums.utils.MyBatisConnectionUtils;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {
    @Bean
    public SqlSessionFactory sqlSessionFactory() {
        MyBatisConnectionUtils.createSqlSessionFactory();
        return MyBatisConnectionUtils.getSqlSessionFactory();
    }
}
