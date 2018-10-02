// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.
import { KeycloakConfig } from 'keycloak-angular';


export function saveServerUrls(urls) {
  environment.mockServerUrl = urls.MOCK_CONSENT_CONFIRMATION_URI;
  environment.aspspXs2aServerUrl = urls.Xs2aServerUrl;
  environment.consentManagementServerUrl = urls.CM_CONSENT_URI;
  environment.profileServerUrl = urls.aspspProfileServerUrl;
  environment.keycloak = urls.keyloakConfig;
}

// Add here your keycloak setup infos
var keycloakConfig: KeycloakConfig = {
  url: 'http://localhost:8081/auth/',
  realm: 'xs2a',
  clientId: 'aspsp-mock'
};

export var environment = {
  production: false,
  consentManagementServerUrl: 'http://localhost:38080/api/v1',
  aspspXs2aServerUrl: 'http://localhost:8080',
  mockServerUrl: 'http://localhost:28080/consent/confirmation',
  profileServerUrl: 'http://localhost:48080/api/v1/aspsp-profile',
  assets: { dotaImages: 'https://api.opendota.com/apps/dota2/images' },
  apis: { dota: 'https://api.opendota.com/api' },
  keycloak: keycloakConfig,
  xRequestId: '2f77a125-aa7a-45c0-b414-cea25a116035',
  tppQwacCertificate: 'qwac certificate',
};
