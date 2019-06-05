/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elvino.oauth2.impl;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	private Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
	
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    	log.debug("loadUserByUsername username="+username);
    	
    	// Saat ini masih blm menerapkan DB, jadi hanya menggunakan data dummy untuk
    	// username : 'username',
    	// password : 'password'
    	
    	return new org.springframework.security.core.userdetails.User(
			"username", 			
			"$2a$10$H72fz/WIK/qzAFpoVglQb.TGEUc3l.dqqEiWNvEV6mgMR3sBT8vGm",  //password
			true, 
			true, 
			true, 
			true, 
			new ArrayList<GrantedAuthority>());
        
    }

}
