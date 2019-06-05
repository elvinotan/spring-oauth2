# Spring-oauth2
Pada project ini menjelaskan bagaimana cara membuat Security OAuth2 server. Sebenarnya Oauth2 dapat di pisah menjadi 2 server yaitu  Authorization Server dan Authentication Server, namun pada project ini kita akan gabung. Setelah fungsi server ini telah berhasil, kita akan mencoba untuk mendaftarkan pada eureka dan zull, sehingga microservices yang lain bisa mengaksesknya lewat zull proxy.

# Penerapan
Penerapan Security Aouth2 ini bermacam macam bentuknya, tergantung kebijaksanaan masing masing, contoh penerapan
a. lakukan pengecekan pertama kali saja, begitu sukses maka komunikasi antar microservices bebas tanpa security</br>
b. selalu  lakukan pengecekan setiap mengakses microservices endpooint</br>

# Target
Dapat mengenerate token, refresh token, check token, autorize url
```
{
    "access_token": "e559fb50-b21f-448f-ae95-0a583dcd5f4f",
    "token_type": "bearer",
    "refresh_token": "16b9a913-84f4-473a-b92d-67b4c804eb52",
    "expires_in": 3409,
    "scope": "read write trust"
}
```

# Dependencies
Oauth2 Server ini akan di daftarkan pada server config, sehingga kita butuh config dependencies
oauth2</br>
Jpa = untuk fetch data dari database dan authenticate dgn user password</br>
Web = Agar bisa mengexpose /oauth url</br>
security = untuk mekanisme UserServiceDetail</br>
Config Client = Untuk register ke Config Server</br>
Eureka Discovery = Untuk register ke rureka</br>
H2 = untuk database</br>

# How to
Pada pembuatan project ini di meski kita mencantumkan dependencies H2, namun kita tidak mengimplementasinya, kita akan menggunakan data dummy untuk kemudahan, untuk penerapan H2 bisa di lihat di spring-rest.</br>
Pada tahapan pertama ini kita akan buat standalone Aouth2</br>
Pada tahap kedua kita akan meng-integrasikan dengan zull, sehingga bisa di access oleh siapa pun</br>

Tahapan Pertama</br>
1. Buat bootstrap.yml untuk connect spring config
```
---
spring:
  application:
    name: SpringOauth2
  cloud:
    config:
      uri: http://localhost:9080
      failFast: false
app:
  login:
    type: DB

#LDAP, DB
```
dilanjutkan dengan buat SpringOauth2.yml di sisi config
```
---
server:
  port: 9091
  address: 0.0.0.0

logging:
  level:
    com:
      simian: DEBUG
      
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:9083/eureka      

# LDAP, DB
app:
  login:
    type: DB   
```
2. Buat SpringBoot Application, karena kita membutuhkan PasswordEncoder, untuk check password valid atau tidak, kita letakan di class ini
```
@SpringBootApplication
@EnableDiscoveryClient
public class SpringOauth2Application {

    public static void main(String[] args) {
	SpringApplication.run(SpringOauth2Application.class, args);
    }
	
    @Bean
    public PasswordEncoder getPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }  

}
```
3. Buat class untuk Autorization Server
```
/**
 * Configures the authorization server.
 * The @EnableAuthorizationServer annotation is used to configure the OAuth 2.0 Authorization Server mechanism,
 * together with any @Beans that implement AuthorizationServerConfigurer (there is a handy adapter implementation with empty methods).
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager; //di ambil dari CustomAuthenticationProvider

    @Autowired
	private CustomUserDetailsService userDetailService; 

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
        	.authenticationManager(authenticationManager) // iCustomAuthenticationProvider
            .userDetailsService(userDetailService) //inject userDatailService
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
        .authorizedGrantTypes("client_credentials", "authorization", "password", "refresh_token") //client_credentials=ambil data client, password=untuk validate user, refresh_token untuk ambil token baru
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
```
4. Buat class Authorization  untuk 
```
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
```
5. Pada aplikasi ini terdapat 2 jenis login DB dan LDAP</br>
a. DB = Cek user berdasarkan DB </br>
b. LDAP = Cek user berdasarkan LDAP third party framework</br> ```Lihat file ActiveDirectory.java untuk cara query ke LDAP```
But Implementasi dari AUthenticationManager, dimana authenticate dilakukan secara manual oleh develper
```

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider{
	private Logger log = LoggerFactory.getLogger(CustomAuthenticationProvider.class);
	
	private final String LOGIN_TYPE_LDAP = "LDAP";
	private final String LOGIN_TYPE_DB = "DB";
	
	@Autowired
	private CustomUserDetailsService customUserDetailService;
	
	@Autowired
    protected PasswordEncoder passwordEncoder;
	
	@Value("${app.login.type}")
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

	@Override
	public boolean supports(Class<?> authentication) {
		 return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
```
5. Testing dengan menggunakan postman</br>
```
Note : Cara ambil token dengan passing username dan password
Url : http://localhost:8080/oauth/token
Grant Type : password
Authorization : Basic Auth (client_id, client_secreat)
Header : Content Type application/x-www-form-urlencoded
Body : grant_type password, password password, username username (Note harus return yang sama dari UserDetail ya kalo tidak error Bad Credential, passwordnya di encode dgn PasswrodEncoder)
Response : {
    "access_token": "42991f14-5af3-419c-b9af-2b41437fc0e8",
    "token_type": "bearer",
    "refresh_token": "c173a2c3-c6c7-4bfb-9e14-9fc34f065cb9",
    "expires_in": 4669,
    "scope": "read write trust"
}
```
```
Note : Cara refresh token yang sudah ada
Url : http://localhost:8080/oauth/token
Grant Type : refresh_token
Authorization : Basic Auth (client_id, client_secreat)
Header : Content Type application/x-www-form-urlencoded
Body : grant_type refresh_token, refresh_token refresh_token
Response : { "access_token": "3389ac46-4b4d-43cd-a2b3-46916690dd07", "token_type": "bearer", "refresh_token": "74c0298a-b35a-4a07-a6d3-8fdfed376825", "expires_in": 4888, "scope": "read write trust" }
*/
```
