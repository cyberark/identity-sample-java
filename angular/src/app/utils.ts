export enum AuthorizationFlow {
  OAUTH = "OAUTH",
  OIDC = "OIDC"
}

export enum OAuthFlow {
  auth = "auth",
  implicit = "implicit",
  authPKCE = "authPKCE",
}

export enum GrantType {
  authorization_code = "authorization_code",
  client_credentials = "client_credentials",
  password = "password"
}

export class PKCEMetaData {
  codeVerifier: string;
  codeChallenge: string;
}

export class AuthorizationMetadataRequest extends PKCEMetaData {
  authFlow: AuthorizationFlow = AuthorizationFlow.OIDC;
  clientId: string;
  clientSecret: string;
  responseType: string = "code";
}

export class TokenMetadataRequest extends PKCEMetaData {
  authFlow: AuthorizationFlow = AuthorizationFlow.OIDC;
  authorizationCode: string;
  grantType: GrantType = GrantType.authorization_code;
  userName: string;
  password: string;
  clientId: string;
  clientSecret: string;
}

/**
 * Fetches the authorization URL
 * @param authRequest AuthorizationMetadataRequest
 * @param context any - The component class context
 */
export const buildAuthorizeURL = (authRequest: AuthorizationMetadataRequest, context: any) => {
  context.authorizationService.buildAuthorizeURL(authRequest).subscribe(
    data => {
      context.loading = false;
      context.authURL = data.Result.authorizeUrl;
      context.authorizeBtn.nativeElement.disabled = false;
    },
    error => {
      console.error(error);
      context.loading = false;
    }
  );
}