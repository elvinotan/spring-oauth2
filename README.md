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
oauth2
Jpa = untuk fetch data dari database dan authenticate dgn user password
Web = Agar bisa mengexpose /oauth url
security = untuk mekanisme UserServiceDetail
Config Client = Untuk register ke Config Server
Eureka Discovery = Untuk register ke rureka
H2 = untuk database

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

```
SpringOauth2.yml
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
