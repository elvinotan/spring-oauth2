package com.elvino.oauth2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.elvino.oauth2.config.CustomAuthenticationProvider;

@SpringBootApplication
public class SpringOauth2Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringOauth2Application.class, args);
	}
	
    @Bean
    public PasswordEncoder getPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }  

}

//Saat ini GantType ada 3 : client_credential, password, refresh_token
//client_credential simpel sudah jalan
/*
* Url : http://localhost:8080/oauth/token
* Grant Type : password
* Authorization : Basic Auth (Username, Password)
* Header : Content Type application/x-www-form-urlencoded
* Body : grant_type password, password password, username username (Note harus return yang sama dari UserDetail ya kalo tidak error Bad Credential, passwordnya di encode dgn PasswrodEncoder)
* Response : { "access_token": "d9a6c617-1bec-46eb-b294-96fb4008d724", "token_type": "bearer", "refresh_token": "74c0298a-b35a-4a07-a6d3-8fdfed376825", "expires_in": 4888, "scope": "read write trust" }
*/

/*
* Url : http://localhost:8080/oauth/token
* Grant Type : refresh_token
* Authorization : Basic Auth (Username, Password)
* Header : Content Type application/x-www-form-urlencoded
* Body : grant_type refresh_token, refresh_token refresh_token
* Response : { "access_token": "3389ac46-4b4d-43cd-a2b3-46916690dd07", "token_type": "bearer", "refresh_token": "74c0298a-b35a-4a07-a6d3-8fdfed376825", "expires_in": 4888, "scope": "read write trust" }
*/