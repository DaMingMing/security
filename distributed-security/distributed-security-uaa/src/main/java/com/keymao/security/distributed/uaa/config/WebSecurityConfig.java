package com.keymao.security.distributed.uaa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //安全拦截机制（最重要）
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable() //屏蔽CSRF
                .authorizeRequests()
/*                .antMatchers("/r/r1").hasAuthority("p1")
                .antMatchers("/r/r2").hasAuthority("p2")*/
                .antMatchers("/r/**").authenticated()//所有/r/**的请求必须认证通过
                .anyRequest().permitAll()//除了/r/**，其它的请求可以访问
                .and()
                .formLogin()//允许表单登录
                .loginPage("/login-view")//自定义登录页面,spring security以重定向的方式跳转到/login-view
                .loginProcessingUrl("/login")//指定登录处理的URL，也就是用户名密码表单提交的目的路径
                .successForwardUrl("/login-success")//自定义登录成功的页面地址
                .and()
                .logout() //自定义退出
                .logoutUrl("/logout") //设置触发退出操作的URL
                .logoutSuccessUrl("/login-view");//退出之后跳转的URL

    }
}
