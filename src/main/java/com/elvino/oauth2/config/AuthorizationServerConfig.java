package com.elvino.oauth2.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;

/**
 * Configures the authorization server.
 * The @EnableAuthorizationServer annotation is used to configure the OAuth 2.0 Authorization Server mechanism,
 * together with any @Beans that implement AuthorizationServerConfigurer (there is a handy adapter implementation with empty methods).
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Setting up the endpointsconfigurer authentication manager.
     * The AuthorizationServerEndpointsConfigurer defines the authorization and token endpoints and the token services.
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .authenticationManager(authenticationManager) // ini menandakan untuk authentication kita handle sendiri
                .exceptionTranslator(loggingExceptionTranslator()); // agar muncul logging lebih detail 
    }

    /**
     * Setting up the clients with a clientId, a clientSecret, a scope, the grant types and the authorities.
     * @param clients
     * @throws Exception
     * Bedakan antara username dan password yang di input oleh user dengan oauth2 username dan password
     * ini di bawah merupakan konfigurasi username dan password untuk oauth2, yang di handle di memory dan bisa juga secara db
     * saat ini kita mendukung 3 tipe jenis grant_type yang bisa di ambil oleh user, antara lain :
     * 1. client_credentials => yang ini kayaknya agar dapat access ke user credential, tp gax tau caranya
     * 2. password => yang ini harus success saat compare dgn userdb, tujuannya untuk authorize user login
     * 3. refresh_login => ini untuk melakukan refersh_login, refersh_login biasanya di gunakan untuk process submit baru
     * untuk role kita ada 2 yaitu ROLE_CLIENT dan ROLE_TRUSTED_CLIENT, tp kayaknya kita gax pake ini, kita pake mekanisme bisa access dan tidak bisa access aja
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
                .inMemory()
                .withClient("simian") // withclient dan secret di gunakan pada saat login dimana Basic Auth menggunakan ini
                .secret(passwordEncoder.encode("k3mb4nggul4"))
                .authorizedGrantTypes("client_credentials", "authorization", "password", "refresh_token") //cleint_credentials=ambil data client, password=untuk validate user, refresh_token untuk ambil token baru
                .authorities("ROLE_CLIENT","ROLE_TRUSTED_CLIENT")
                .scopes("read", "write", "trust")
                .resourceIds("oauth2-resource")
                .accessTokenValiditySeconds(5000); // valid access_token dalam detik
    }

    /**
     * We here defines the security constraints on the token endpoint.
     * We set it up to isAuthenticated, which returns true if the user is not anonymous
     * @param security the AuthorizationServerSecurityConfigurer.
     * @throws Exception
     * Ini untuk cek user tersebut authenticate atau tidak dari priciple
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.checkTokenAccess("isAuthenticated()"); // Cek token apakah sudah terAuthenticated
    }


    @Bean
    public WebResponseExceptionTranslator loggingExceptionTranslator() { // Hanya untuk menampilka logger agar lebih bisa terbaca
        return new DefaultWebResponseExceptionTranslator() {
        	
            @Override
            public ResponseEntity<OAuth2Exception> translate(Exception e) throws Exception {
                // This is the line that prints the stack trace to the log. You can customise this to format the trace etc if you like
                e.printStackTrace();

                // Carry on handling the exception
                ResponseEntity<OAuth2Exception> responseEntity = super.translate(e);
                HttpHeaders headers = new HttpHeaders();
                headers.setAll(responseEntity.getHeaders().toSingleValueMap());
                OAuth2Exception excBody = responseEntity.getBody();
                return new ResponseEntity<>(excBody, headers, responseEntity.getStatusCode());
            }
        };
    }
}