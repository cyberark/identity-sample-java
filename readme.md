# CyberArk Identity Demo Web App (Java)
**Status**: Community

Acme Inc. is an imaginary company using CyberArk Identity APIs, SDKs, and widgets to secure its web applications. This playground application shows all the possible variations that a developer from Acme has at her disposal for this. This app uses Java Spring backend and Angular JS frontend.

![Certification Level Community](https://camo.githubusercontent.com/fc39ec5a52592c929ecd6e7ff4e3d1b7d5a4856c512a5486a5c24a00db6bcf6d/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f43657274696669636174696f6e2532304c6576656c2d436f6d6d756e6974792d3238413734353f6c696e6b3d68747470733a2f2f6769746875622e636f6d2f637962657261726b2f636f6d6d756e6974792f626c6f622f6d61737465722f436f6e6a75722f636f6e76656e74696f6e732f63657274696669636174696f6e2d6c6576656c732e6d64)

## Usage
### Tenant Setup
#### Role, Policies and Authentication Profile
* Add a Role named MFA in the admin portal and give User Management, Role Management Rights.
* Link for further reference [Role Creation](https://docs.cyberark.com/Product-Doc/OnlineHelp/Idaptive/Latest/en/Content/CoreServices/GetStarted/RolesAdd.htm)
* Create a policy and add the MFA role to the policy
* Under Authentication Policies -> CyberArk Identity, setup Authentication profile

#### OIDC Client App
* Create a OIDC Connect app
* Navigate to Trust Section and give OpenID Connect Client Secret value
* Add Resource application URL as https://apidemo.cyberark.app:4200/oidcflow
* Add the Redirect destinations as https://apidemo.cyberark.app:4200/RedirectResource
* Under Permissions tab, add the MFA role created in the previous steps and give Run Access to generate Tokens.

#### OAuth Client App
* Create a OAuth 2.0 client app
* Select Client ID Type as Confidential
* Add the following Redirect destinations: 
    https://apidemo.cyberark.app:8080/RedirectResource
    https://apidemo.cyberark.app:4200/RedirectResource
* Under Tokens tab, select Auth Code, Implicit, Client Creds, Resource Owner & Issue refresh tokens checkboxes
* Under Scope tab, Add a scope "all" with .* as REST Regex
* Under Permissions tab, add the MFA role created in the previous steps and give Run access to generate tokens

#### Create a confidential client
* Add a new user and give service_user as the login name and display name
* Under status, check password never expires, is service user & Is OAuth confidential client checkboxes
* To the MFA role created in the previous steps, add this confidential client

#### Trusted DNS for API calls
* Navigate to Settings -> Authentication -> Security Settings -> API Security in the admin portal
* Add an entry under Trusted DNS Domains for API Calls - apidemo.cyberark.app

For further reference
* Frontend - [Angular](./angular/README.md)
* Backend - [Spring-boot](./spring-boot/README.md)

## Code Maintainers
CyberArk Identity Team

<a id="license"></a>
## License
This project is licensed under Apache License 2.0 - see [`LICENSE`](LICENSE) for more details.

Copyright (c) 2021 CyberArk Software Ltd. All rights reserved.
