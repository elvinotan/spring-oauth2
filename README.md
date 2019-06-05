# Spring-oauth2
Pada project ini menjelaskan bagaimana cara membuat Security OAuth2 server. Sebenarnya Oauth2 dapat di pisah menjadi 2 server yaitu  Authorization Server dan Authentication Server, namun pada project ini kita akan gabung. Setelah fungsi server ini telah berhasil, kita akan mencoba untuk mendaftarkan pada eureka dan zull, sehingga microservices yang lain bisa mengaksesknya lewat zull proxy.

# Penerapan
Penerapan Security Aouth2 ini bermacam macam bentuknya, tergantung kebijaksanaan masing masing, contoh penerapan
a. lakukan pengecekan pertama kali saja, begitu sukses maka komunikasi antar microservices bebas tanpa security
b. selalu  lakukan pengecekan setiap mengakses microservices endpooint

# Target
Dapat mengenerate token, refresh token, check token, autorize url


