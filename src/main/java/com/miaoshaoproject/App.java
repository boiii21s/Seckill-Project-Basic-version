package com.miaoshaoproject;

import com.miaoshaoproject.dao.UserDOMapper;
import com.miaoshaoproject.dataobject.UserDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */

@RestController
@SpringBootApplication(scanBasePackages = {"com.miaoshaoproject"})
@MapperScan("com.miaoshaoproject.dao")
public class App {
    @Autowired(required = false)
    private UserDOMapper userDOMapper;

    @GetMapping("/")
    public String home(){
        UserDO userDO = userDOMapper.selectByPrimaryKey(1);
        if (userDO == null){
            return "用户不存在";
        }else{
            return userDO.toString();
        }

    }

    public static void main( String[] args ) {
        SpringApplication.run(App.class,args);
        System.out.println( "Hello World!" );
    }
}
