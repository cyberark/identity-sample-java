#!/bin/bash

CERTS_DIR=./certs/

# Navigate to certs directory
cd "${CERTS_DIR}"

# OpenSSL commands in case the certificate expired or not working
# openssl req -x509 -nodes -new -sha256 -days 2048 -newkey rsa:2048 -keyout RootCA.key -out RootCA.pem -subj "/C=US/CN=identitydemo.acmeinc.com"
#
# openssl x509 -outform pem -in RootCA.pem -out RootCA.crt
#
# openssl req -new -nodes -newkey rsa:2048 -keyout server.key -out server.csr -subj "/CN=server.local"
#
# openssl x509 -req -sha256 -days 2048 -in server.csr -CA RootCA.pem -CAkey RootCA.key -CAcreateserial -extfile "certificate.cnf" -out server.crt
# 
# openssl pkcs12 -export -out sslkeystore.p12 -inkey server.key -in server.crt -name sampleapp -passout pass:"<PASSWORD>"

ROOT_CA_FILE=./RootCA.crt
SSL_KEYSTORE_FILE=./sslkeystore.p12
SPRING_BOOT_RESOURCES_DIR=../spring-boot/src/main/resources/
PWD=`pwd`
ANGULAR_SSL_DIR=$PWD/../angular/ssl/
SERVER_CRT_FILE=./server.crt
SERVER_CRT_KEY=./server.key
UBUNTU_CA_CERTS_DIR=/usr/share/ca-certificates

# Permission changes to certificate files
chmod -R 755 *

# Copy .p12 to server
if [ -f "$SSL_KEYSTORE_FILE" ]; then
  cp "$SSL_KEYSTORE_FILE" "$SPRING_BOOT_RESOURCES_DIR"
fi 

# Copy SSL certificates
if [ -d "$ANGULAR_SSL_DIR" ]; then
    echo "Already exists!!"
else
  mkdir -p "$ANGULAR_SSL_DIR"
  echo "Created Angular SSL Directory!!"
  cp "$SERVER_CRT_FILE" "$ANGULAR_SSL_DIR"
  cp "$SERVER_CRT_KEY" "$ANGULAR_SSL_DIR"
fi

# Update hosts
ip_address="127.0.0.1"
host_name="identitydemo.acmeinc.com"
# Find existing entries in the hosts file and save the line numbers
matching_in_hosts="$(grep -n $host_name /etc/hosts | cut -f1 -d:)"
host_entry="${ip_address} ${host_name}"

echo "Please enter the password if prompted."

if [ ! -z "$matching_in_hosts" ]
then
    echo "Already hosts entry exists !!"
else
    echo "Adding the hosts entry"
    echo "$host_entry" | sudo tee -a /etc/hosts > /dev/null
fi

case "$(uname -s)" in

   Darwin)
    echo 'Mac OS X'
    # MacOSX
    sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain "$ROOT_CA_FILE"
    sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain "$SERVER_CRT_FILE"
     ;;

   Linux)
    echo 'Linux'
    # Ubuntu
    sudo cp "$ROOT_CA_FILE" "$UBUNTU_CA_CERTS_DIR"
    sudo cp "$SERVER_CRT_FILE" "$UBUNTU_CA_CERTS_DIR"

    cd "$UBUNTU_CA_CERTS_DIR"

    sudo dpkg-reconfigure ca-certificates

     ;;

   CYGWIN*|MINGW32*|MSYS*|MINGW*|*Windows*)
    echo 'MS Windows'
    echo 'Run windows_prerequisites.bat file from Windows Command Prompt with Admin previliges'
     ;;

   # Add here more strings to compare
   # See correspondence table at the bottom of this answer

   *)
     echo 'Other OS' 
     ;;
esac
