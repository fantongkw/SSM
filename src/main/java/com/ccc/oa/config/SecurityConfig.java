package com.ccc.oa.config;

import com.ccc.oa.security.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);
    private final CustomUserDetailsService customUserDetailsService;
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Autowired
    public SecurityConfig(CustomUserDetailsService customUserDetailsService, FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        this.customUserDetailsService = customUserDetailsService;
        this.sessionRepository = sessionRepository;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                    /*.antMatchers("/registered").permitAll()
                    .antMatchers("/personal/**").hasAnyRole("ROLE_USER")
                    .antMatchers("/app/**").hasAnyRole("ROLE_USER")
                    .antMatchers("/dept/**").hasAnyRole("ROLE_DEPTS")
                    .antMatchers("/user/**").hasAnyRole("ROLE_USERS")
                    .antMatchers("/role/**").hasAnyRole("ROLE_ROLES")
                    .antMatchers("/notice").hasAnyRole("ROLE_USER")*/
                    .antMatchers("/**").permitAll()
                    .and()
                .sessionManagement()
                    .maximumSessions(1)
                        .sessionRegistry(sessionRegistry())
                        .and()
                    .and()
                .formLogin()
                    .loginPage("/login")
                    .failureUrl("/login-error")
                    .permitAll()
                    .and()
                .rememberMe()
                    .and()
                .logout()
                    .logoutSuccessUrl("/login-logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
                    .and()
                .rememberMe()
                    .and()
                .httpBasic();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SpringSessionBackedSessionRegistry sessionRegistry() {
        return new SpringSessionBackedSessionRegistry<>(this.sessionRepository);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public HttpFirewall httpFirewall(){
        return new DefaultHttpFirewall();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        web.httpFirewall(httpFirewall());
    }
}