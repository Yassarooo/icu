package com.jazara.icu.auth;

import com.jazara.icu.auth.domain.Role;
import com.jazara.icu.auth.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableZuulProxy
@EnableHystrix
@EnableHystrixDashboard
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class},scanBasePackages = "com.jazara.icu.auth")
@EnableDiscoveryClient
public class AuthApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    RoleService roleService;

    @Override
    public void run(String... strings) throws Exception {

        Role uRole = new Role();
        uRole.setName("USER");
        uRole.setDescription("USER Role (Only Manage owned account)");
        roleService.CreateRole(uRole);
        Role aRole = new Role();
        aRole.setName("ADMIN");
        aRole.setDescription("ADMIN Role (Manage users)");
        roleService.CreateRole(aRole);
    }
}
