package com.sdu.irlab.chatlabelling.security;

import com.sdu.irlab.chatlabelling.service.SystemUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@Configuration
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    private CustomAuthenticationFailHandler customAuthenticationFailHandler;

    @Autowired
    private SecurityInterceptor securityInterceptor;

    @Override
    @Bean
    public UserDetailsService userDetailsService() {
        return new SystemUserDetailsService();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {
        //指定密码加密所使用的加密器为passwordEncoder()
        // 需要将密码加密后写入数据库
        auth.userDetailsService(userDetailsService()).passwordEncoder(getPasswordEncoder());

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration addInterceptor = registry.addInterceptor(securityInterceptor);
        // 排除配置
//        addInterceptor.excludePathPatterns("/error");
//        addInterceptor.excludePathPatterns("/login**");

        // 拦截配置,只拦截api开头的请求
        addInterceptor.addPathPatterns("/api/**");
    }


    @Override
    public void configure(WebSecurity web) throws Exception {
        // 设置不拦截规则
        web.ignoring().antMatchers("/static/**", "/test/**");//忽略静态资源、测试url
//        .and().ignoring().antMatchers("/","/profile","/api/saveProfileAndLogin");//允许填写信息、自动添加用户并登陆
    }

    @Bean
    public static ServletListenerRegistrationBean httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean(new CustomHttpSessionEventPublisher());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //设置拦截规则
        http.authorizeRequests()
                .anyRequest().authenticated();

        //自定义登录界面
        http.csrf().disable().formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/checkLogin")
                .defaultSuccessUrl("/")
                .permitAll()
                //自定义登录成功处理类
                .successHandler(customAuthenticationSuccessHandler)
                //自定义登录失败处理类
                .failureHandler(customAuthenticationFailHandler);

        // 自定义注销
        http.logout().logoutUrl("/logout")
                .logoutSuccessUrl("/login")//注销回到登录页面并且提示登出
                .permitAll()
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true);

        //session管理
//
//        http.sessionManagement()
//                .maximumSessions(1)
//                .maxSessionsPreventsLogin(false)
//                .expiredUrl("/login?expired")
//                .sessionRegistry(sessionRegistry());

    }


    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new CustomPasswordEncoder();
    }

}
