@echo off
cd certs

SET ROOT_CA_KEY=RootCA.key
SET ROOT_CA_PEM=RootCA.pem
SET ROOT_CA_FILE=RootCA.crt
SET SSL_KEYSTORE_FILE=sslkeystore.p12
SET SERVER_CRT_FILE=server.crt
SET SERVER_CRT_KEY=server.key
SET SERVER_CSR=server.csr
SET SERVER_ALIAS=sampleapp

:: OpenSSL commands in case the certificate expired or not working
openssl req -x509 -nodes -new -sha256 -days 1024 -newkey rsa:2048 -keyout %ROOT_CA_KEY% -out %ROOT_CA_PEM% -subj "/C=US/CN=AcmeInc"
:: 
openssl x509 -outform pem -in %ROOT_CA_PEM% -out %ROOT_CA_FILE%
:: 
set RANDFILE=.rnd 
::
openssl req -new -nodes -newkey rsa:2048 -keyout %SERVER_CRT_KEY% -out %SERVER_CSR% -subj "/C=US/ST=TX/L=TEXAS/O=Example-Certificates/CN=identitydemo.acmeinc.com"
:: 
openssl x509 -req -sha256 -days 1024 -in %SERVER_CSR% -CA %ROOT_CA_PEM% -CAkey %ROOT_CA_KEY% -CAcreateserial -extfile "domains.ext" -out %SERVER_CRT_FILE%
:: 
openssl pkcs12 -export -out %SSL_KEYSTORE_FILE% -inkey %SERVER_CRT_KEY% -in %SERVER_CRT_FILE% -name %SERVER_ALIAS% -passout pass:"<PASSWORD>"

if not EXIST ..\angular\ssl\ (
    mkdir ..\angular\ssl\
)

:: Installing Root CA certificate to trusted root certificate store
if EXIST %ROOT_CA_FILE% (
certutil.exe -addstore root .\%ROOT_CA_FILE%
) else (
    ECHO "File RootCA.crt does not exists!!"
)

if EXIST %SERVER_CRT_FILE% (
:: Installing server certificate to personal certificate store
certutil.exe -addstore My .\%SERVER_CRT_FILE%

:: Copying the certificate to angular project
COPY .\%SERVER_CRT_FILE% ..\angular\ssl\
) else (
    ECHO "File server.crt does not exists!!"
)

:: Copying the certificate key to angular project
if EXIST %SERVER_CRT_KEY% (
COPY .\%SERVER_CRT_KEY% ..\angular\ssl\
) else (
    ECHO "File server.key does not exists!!"
)

:: Copying SSL Keystore to spring-boot project
if EXIST %SSL_KEYSTORE_FILE% (
COPY .\%SSL_KEYSTORE_FILE% ..\spring-boot\src\main\resources\
) else (
    ECHO "File sslkeystore.p12 does not exists!!"
)

SET NEWLINE=^& echo.
SET HOSTSPATH=%WINDIR%\system32\drivers\etc\hosts

FIND /C /I "identitydemo.acmeinc.com" %HOSTSPATH%
IF %ERRORLEVEL% NEQ 0 ECHO %NEWLINE%^127.0.0.1 identitydemo.acmeinc.com>>%HOSTSPATH%
ECHO "Hosts file updated"