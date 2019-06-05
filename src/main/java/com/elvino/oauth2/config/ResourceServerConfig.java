package com.elvino.oauth2.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

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
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .headers()
                .frameOptions()
                .disable()
                .and()
            .authorizeRequests().antMatchers("/**").permitAll();
//            	//.antMatchers("/oauth/**").permitAll()
//                .antMatchers("/login").permitAll()
//                .antMatchers("/public/**").permitAll()
//                .antMatchers("/websocket/**").authenticated()
//                .antMatchers("/private/**").authenticated();
    }


}