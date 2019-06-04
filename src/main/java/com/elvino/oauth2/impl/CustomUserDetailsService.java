/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elvino.oauth2.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 *
 * @author developer
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
	private Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

//    @Autowired
//    private MobileService mobileService;
	
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    	log.debug("loadUserByUsername username="+username);
    	
//    	User user = mobileService.findByUsername(username);
//    	if (user == null) return null;
    	
    	List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
//    	for (Role role : user.getRoles()) {
//    		authorities.add(new SimpleGrantedAuthority(role.getName()));
//    	}
    	
//    	return new org.springframework.security.core.userdetails.User(
//    			user.getUsername(), 
//    			user.getPassword(), 
//    			user.isActive(), 
//    			user.isActive(), 
//    			user.isActive(), 
//    			user.isActive(), 
//    			authorities);
    	
    	return new org.springframework.security.core.userdetails.User(
			"username", 
			"password", 
			true, 
			true, 
			true, 
			true, 
			authorities);
        
    }

}
