package com.elvino.oauth2.config;


import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

/**
 * Query Active Directory using Java
 * 
 * @filename ActiveDirectory.java
 * @author <a href="mailto:jeeva@myjeeva.com">Jeevanandam Madanagopal</a>
 * @copyright &copy; 2010-2012 www.myjeeva.com
 */
public class ActiveDirectory {
	
	private static final Pattern SUB_ERROR_CODE = Pattern.compile(".*data\\s([0-9a-f]{3,4}).*");

    // Error codes
    private static final int USERNAME_NOT_FOUND = 0x525;
    private static final int INVALID_PASSWORD = 0x52e;
    private static final int NOT_PERMITTED = 0x530;
    private static final int PASSWORD_EXPIRED = 0x532;
    private static final int ACCOUNT_DISABLED = 0x533;
    private static final int ACCOUNT_EXPIRED = 0x701;
    private static final int PASSWORD_NEEDS_RESET = 0x773;
    private static final int ACCOUNT_LOCKED = 0x775;
    
    private boolean convertSubErrorCodesToExceptions;
    
	// Logger
	private static final Logger LOG = Logger.getLogger(ActiveDirectory.class.getName());

    //required private variables   
    private Properties properties;
    private DirContext dirContext;
    ContextFactory contextFactory = new ContextFactory();
    private SearchControls searchCtls;
	private String[] returnAttributes = { "sAMAccountName", "givenName", "cn", "mail" };
    private String domainBase;
    private String baseFilter = "(&((&(objectCategory=Person)(objectClass=User)))";

    /**
     * constructor with parameter for initializing a LDAP context
     * 
     * @param username a {@link java.lang.String} object - username to establish a LDAP connection
     * @param password a {@link java.lang.String} object - password to establish a LDAP connection
     * @param domainController a {@link java.lang.String} object - domain controller name for LDAP connection
     */
    
    int parseSubErrorCode(String message) {
        Matcher m = SUB_ERROR_CODE.matcher(message);

        if (m.matches()) {
            return Integer.parseInt(m.group(1), 16);
        }

        return -1;
    }
    
    static class ContextFactory {
        DirContext createContext(Hashtable<?,?> env) throws NamingException {
            return new InitialLdapContext(env, null);
        }
    }
    
    String raiseExceptionForErrorCode(int code) {
        switch (code) {
            case PASSWORD_EXPIRED:
                return "User credentials have expired";
            case ACCOUNT_DISABLED:
            	return"User is disabled";
            case ACCOUNT_EXPIRED:
            	return"User account has expired";
            case ACCOUNT_LOCKED:
            	return"User account is locked";
        }
		return "User not found";
    }
    
     String handleBindException(String bindPrincipal, NamingException exception) {

        int subErrorCode = parseSubErrorCode(exception.getMessage());

        if (subErrorCode > 0) {
        	LOG.info("Active Directory authentication failed: " + subCodeToLogMessage(subErrorCode));

            if (convertSubErrorCodesToExceptions) {
               return raiseExceptionForErrorCode(subErrorCode);
            }else{
            	return subCodeToLogMessage(subErrorCode);
            }
        } else {
        	return "Failed to locate AD-specific sub-error code in message";
        }
    }
    
    String subCodeToLogMessage(int code) {
        switch (code) {
            case USERNAME_NOT_FOUND:
                return "User not Found";
            case INVALID_PASSWORD:
                return "Invalid Username/Password";
            case NOT_PERMITTED:
                return "User not permitted to logon at this time";
            case PASSWORD_EXPIRED:
                return "Password has expired";
            case ACCOUNT_DISABLED:
                return "Account is disabled";
            case ACCOUNT_EXPIRED:
                return "Account expired";
            case PASSWORD_NEEDS_RESET:
                return "User must reset password";
            case ACCOUNT_LOCKED:
                return "Account locked";
        }

        return "Unknown (error code " + Integer.toHexString(code) +")";
    }
    
    public void setConvertSubErrorCodesToExceptions(boolean convertSubErrorCodesToExceptions) {
        this.convertSubErrorCodesToExceptions = convertSubErrorCodesToExceptions;
    }
    
    /*public ActiveDirectory(String username, String password, String domain, String domainAlias, String host) {
        properties = new Properties();        
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        properties.put(Context.PROVIDER_URL, "LDAP://" + host);
        if(domainAlias == null)
        	properties.put(Context.SECURITY_PRINCIPAL, username + "@" + domain);
        else
        	properties.put(Context.SECURITY_PRINCIPAL, username + "@" + domainAlias);
        properties.put(Context.SECURITY_CREDENTIALS, password);
        //properties.put(Context.SECURITY_AUTHENTICATION, "simple");
        properties.put(Context.REFERRAL,"follow");
        
        //initializing active directory LDAP connection
        try {
        	dirContext = new InitialDirContext(properties);
		} catch (Exception e) {
			LOG.severe(e.getMessage());
		}
        
        
        //default domain base for search
        domainBase = getDomainBase(domain);
        
        //initializing search controls
        searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setReturningAttributes(returnAttributes);
    }*/
    
    public ActiveDirectory(String username, String password, String domain, String domainAlias, String host) throws Exception {
        try{
	    	properties = new Properties();        
	        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	        properties.put(Context.PROVIDER_URL, "LDAP://" + host);
	        if(domainAlias == null)
	        	properties.put(Context.SECURITY_PRINCIPAL, username + "@" + domain);
	        else
	        	properties.put(Context.SECURITY_PRINCIPAL, username + "@" + domainAlias);
	        properties.put(Context.SECURITY_CREDENTIALS, password);
	        //properties.put(Context.SECURITY_AUTHENTICATION, "simple");
	        properties.put(Context.REFERRAL,"follow");
			
			//initializing active directory LDAP connection
			 try {
				 System.out.println("AAAAAAAAAAAAAAAAAAA");
				dirContext = new InitialDirContext(properties);
				System.out.println("BBBBBBBBBBBBBBBBBb");
			} catch (NamingException e) {
				//LOG.severe(e.getMessage());
				//throw new Exception(e);
				if ((e instanceof AuthenticationException) || (e instanceof OperationNotSupportedException)) {
					if(domainAlias == null)
						throw new Exception( handleBindException(username + "@"+domain, e));
					else
						throw new Exception( handleBindException(username + "@"+domainAlias, e));
	                //throw new Exception(badCredentials());
	            } else {
	            	throw new Exception(e.getMessage());
	            }
			}
			
			//default domain base for search
			domainBase = getDomainBase(domainAlias);
			
			//initializing search controls
			searchCtls = new SearchControls();
			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchCtls.setReturningAttributes(returnAttributes);
		}catch(Exception e){
    	   throw new Exception(e.getMessage());
       }
    }
    
    /**
     * search the Active directory by username/email id for given search base
     * 
     * @param searchValue a {@link java.lang.String} object - search value used for AD search for eg. username or email
     * @param searchBy a {@link java.lang.String} object - scope of search by username or by email id
     * @param searchBase a {@link java.lang.String} object - search base value for scope tree for eg. DC=myjeeva,DC=com
     * @return search result a {@link javax.naming.NamingEnumeration} object - active directory search result
     * @throws NamingException
     */
    public NamingEnumeration<SearchResult> searchUser(String searchValue, String searchBy, String searchBase) throws NamingException {
    	String filter = getFilter(searchValue, searchBy);    	
    	String base = (null == searchBase) ? domainBase : getDomainBase(searchBase); // for eg.: "DC=myjeeva,DC=com";
		return this.dirContext.search(base, filter, this.searchCtls);
    }

    /**
     * closes the LDAP connection with Domain controller
     */
    public void closeLdapConnection(){
        try {
            if(dirContext != null)
                dirContext.close();
        }
        catch (NamingException e) {
        	LOG.severe(e.getMessage());            
        }
    }
    
    /**
     * active directory filter string value
     * 
     * @param searchValue a {@link java.lang.String} object - search value of username/email id for active directory
     * @param searchBy a {@link java.lang.String} object - scope of search by username or email id
     * @return a {@link java.lang.String} object - filter string
     */
    private String getFilter(String searchValue, String searchBy) {
    	String filter = this.baseFilter;    	
    	if(searchBy.equals("email")) {
    		filter += "(mail=" + searchValue + "))";
    	} else if(searchBy.equals("username")) {
    		filter += "(sAMAccountName=" + searchValue + "))";
    	}
		return filter;
	}
    
    /**
     * creating a domain base value from domain controller name
     * 
     * @param base a {@link java.lang.String} object - name of the domain controller
     * @return a {@link java.lang.String} object - base name for eg. DC=myjeeva,DC=com
     */
	private static String getDomainBase(String base) {
		char[] namePair = base.toUpperCase().toCharArray();
		String dn = "DC=";
		for (int i = 0; i < namePair.length; i++) {
			if (namePair[i] == '.') {
				dn += ",DC=" + namePair[++i];
			} else {
				dn += namePair[i];
			}
		}
		return dn;
	}
}