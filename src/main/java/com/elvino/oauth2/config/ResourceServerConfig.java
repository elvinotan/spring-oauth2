package com.elvino.oauth2.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

import com.elvino.oauth2.impl.CustomUserDetailsService;

/**
 *The @EnableResourceServer annotation adds a filter of type OAuth2AuthenticationProcessingFilter automatically
 *to the Spring Security filter chain.
 * 
 * Class ini untuk menghadle request yang masuk 
 * untuk path /private/** harus di authenticated. Contoh : http://localhost:8080/medallionSalesMobile/private/account?access_token=abcdefghijklmanobqrstuvwxyz
 * untuk path /websocket/** harus di authenticated. Contoh : http://localhost:8080/medallionSalesMobile/websocket/getNav?access_token=abcdefghijklmanobqrstuvwxyz
 * selain ke 2 path tersebut maka tidak perlu di authenticated
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private CustomUserDetailsService userDetailService;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .headers()
                .frameOptions()
                .disable()
                .and()
            .authorizeRequests()
                .antMatchers("/login").permitAll()
                .antMatchers("/public/**").permitAll()
                .antMatchers("/websocket/**").authenticated()
                .antMatchers("/private/**").authenticated()
                .and().rememberMe().key("uniqeAndSecret").userDetailsService(userDetailService);
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
    @Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailService);
	}
}