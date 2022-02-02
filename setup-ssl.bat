@echo off

:: OpenSSL commands in case the certificate expired or not working
:: openssl req -x509 -nodes -new -sha256 -days 2048 -newkey rsa:2048 -keyout RootCA.key -out RootCA.pem -config "certificate.cnf"
:: 
:: openssl x509 -outform pem -in RootCA.pem -out RootCA.crt
:: 
:: openssl req -new -nodes -newkey rsa:2048 -keyout server.key -out server.csr -subj "/CN= server.local"
:: 
:: openssl x509 -req -sha256 -days 2048 -in server.csr -CA RootCA.pem -CAkey RootCA.key -CAcreateserial -extfile "certificate.cnf" -out server.crt
:: 
:: openssl pkcs12 -export -out sslkeystore.p12 -inkey server.key -in server.crt -name sampleapp -passout pass:"<PASSWORD>"

cd certs

if not EXIST ..\angular\ssl\ (
    mkdir ..\angular\ssl\
)

:: Installing Root CA certificate to trusted root certificate store
if EXIST RootCA.crt (
certutil.exe -addstore root .\RootCA.crt
) else (
    ECHO "File RootCA.crt does not exists!!"
)

if EXIST server.crt (
:: Installing server certificate to personal certificate store
certutil.exe -addstore My .\server.crt

:: Copying the certificate to angular project
COPY .\server.crt ..\angular\ssl\
) else (
    ECHO "File server.crt does not exists!!"
)

:: Copying the certificate key to angular project
if EXIST server.key (
COPY .\server.key ..\angular\ssl\
) else (
    ECHO "File server.key does not exists!!"
)

:: Copying SSL Keystore to spring-boot project
if EXIST sslkeystore.p12 (
COPY .\sslkeystore.p12 ..\spring-boot\src\main\resources\
) else (
    ECHO "File sslkeystore.p12 does not exists!!"
)

SET NEWLINE=^& echo.
SET HOSTSPATH=%WINDIR%\system32\drivers\etc\hosts

FIND /C /I "identitydemo.acmeinc.com" %HOSTSPATH%
IF %ERRORLEVEL% NEQ 0 ECHO %NEWLINE%^127.0.0.1 identitydemo.acmeinc.com>>%HOSTSPATH%
ECHO "Hosts file updated"