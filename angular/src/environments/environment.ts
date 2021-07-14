// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
  enableMFAWidgetFlow: true,
  baseUrl:"https://apidemo.cyberark.app:8080/",
  apiFqdn:"<YOUR_TENANT_FULLY_QUALIFIED_DOMAIN_NAME>",
  oauthAuthCodeFlowAppId: "<YOUR_OAUTH_APPLICATION_ID_FOR_AUTHCODE_FLOW>",
  oauthAuthCodeFlowSco: "<YOUR_OAUTH_SCOPE_FOR_AUTHCODE_FLOW>",
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
