package com.elvino.oauth2.config;


import java.util.ArrayList;

import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.elvino.oauth2.impl.CustomUserDetailsService;

@Component
public class CustomAuthenticationProvider implements AuthenticationManager{
	private Logger log = LoggerFactory.getLogger(CustomAuthenticationProvider.class);
	
	private final String LOGIN_TYPE_LDAP = "LDAP";
	private final String LOGIN_TYPE_DB = "DB";
	
	@Autowired
	private CustomUserDetailsService customUserDetailService;
	
	@Autowired
    protected PasswordEncoder passwordEncoder;
	
	@Value("${medallion.mobile.login.type}")
	private String loginType;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        
        try {
        	log.debug("LOGIN TYPE "+loginType);
	        if (LOGIN_TYPE_DB.equals(loginType)) {
	        	
	        	UserDetails userDetail = customUserDetailService.loadUserByUsername(name);
	        	if (userDetail == null) throw new UsernameNotFoundException("User "+name+" not found");
	        	
	        	boolean match = passwordEncoder.matches(password, userDetail.getPassword());
	        	if (!match) throw new BadCredentialsException("Password is not match");
	        	
	        	return new UsernamePasswordAuthenticationToken(name, password, new ArrayList<>());
	        	
	        }else if (LOGIN_TYPE_LDAP.equals(loginType)) {
	        	
	        	String domain = "ldapdev2.medallion.co";
	    		String alias = "ldapdev2.medallion.co";
	    		String host = "192.168.0.225";
	    		
	    		// User yang ada di ldap adalah
	    		// username : SIMIANUSER1, password : SimianSuper01
	    		// Koneksi ke ldap ini tidak menggunakan framework spring boot, tapi manual
	    		
	        	ActiveDirectory activeDirectory = new ActiveDirectory(name, password, domain, alias, host);
				NamingEnumeration<SearchResult> result = activeDirectory.searchUser(name, "username", null);
				
				if (!result.hasMore()) { throw new UsernameNotFoundException("User "+name+ " not Found"); }
	        	        	
	            return new UsernamePasswordAuthenticationToken(name, password, new ArrayList<>());
	        }else {
	        	
	        	throw new UsernameNotFoundException("Unknown login type");
	        }
        }catch(Exception e) {
        	log.error(e.getMessage(), e);
        	return null;
        }
	}
//
//	@Override
//	public boolean supports(Class<?> authentication) {
//		 return authentication.equals(UsernamePasswordAuthenticationToken.class);
//	}

}