# Angular
## Prerequisite
* Follow [Deployment Guide](https://identity-developer.cyberark.com/docs/sample-app-deployment-guide) to setup the sample app environment.

## Configuration
* After installing all the dependencies, update the placeholder values in angular/src/environments/environment.ts file.
    ```console
    apiFqdn:"<YOUR_TENANT_FULLY_QUALIFIED_DOMAIN_NAME>",
    oauthAppId: "<YOUR_OAUTH_APPLICATION_ID>",
    oauthScope: "<YOUR_OAUTH_SCOPE>"
    ```

* Update the placeholder value `<YOUR_TENANT_FULLY_QUALIFIED_DOMAIN_NAME>` in angular/src/index.html file.
    ```console
    <script src="https://<YOUR_TENANT_FULLY_QUALIFIED_DOMAIN_NAME>/vfslow/lib/uibuild/standalonelogin/login.js"></script>
    <link rel="stylesheet" type="text/css" href="https://<YOUR_TENANT_FULLY_QUALIFIED_DOMAIN_NAME>/vfslow/lib/uibuild/standalonelogin/css/login.css">
    ```

## Install Dependencies 
* Navigate to angular folder
    ```console
    cd angular
    ```

* Install the dependencies
    ```console
    npm i
    ```


## Run the application
```console
npm start
```