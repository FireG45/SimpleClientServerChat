

# SimpleClientServerChat

<img src="https://imgbly.com/ib/Z7ymJfsVR2.png" style="max-width: 100%; max-height: 100vh; width: auto; margin: auto;" alt="">

SimpleClientServerChat is simple chat application that works on Java sockets and uses RC4 encryption with RSA digital signature. SimpleClientServerChat was developed as final project in Information security of computer networks university course.

## Key Features

* RC4 message encryption
* RSA digital signature
* Challenge–response authentication
* Connection with Java socket

## Build
* Java 20 SDK required
* Client: ```cd ClientSideIS && ./mvnw clean install && ./mvnw run```
* Server: ```cd ServerSideIS && ./mvnw clean install && spring-boot:run```
