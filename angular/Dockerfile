FROM nginx:latest
 
COPY default.conf /etc/nginx/conf.d/
COPY ssl/server.crt /etc/ssl/
COPY ssl/server.key /etc/ssl
WORKDIR /usr/share/nginx/html
COPY dist/api-demo .