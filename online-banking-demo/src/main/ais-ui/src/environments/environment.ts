// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.
import { KeycloakConfig } from 'keycloak-angular';

// Add here your keycloak setup infos
const keycloakConfig: KeycloakConfig = {
  url: 'http://localhost:8081/auth/',
  realm: 'xs2a',
  clientId: 'aspsp-mock'
};

export const environment = {
  production: false,
  assets: { dotaImages: 'https://api.opendota.com/apps/dota2/images' },
  apis: { dota: 'https://api.opendota.com/api' },
  aspspConsentServerUrl: 'http://localhost:8080/v1/consents',
  aspspAccountServerUrl: 'http://localhost:8080/v1/accounts',
  cmsServerUrl: 'http://localhost:38080/api/v1/ais/consent',
  mockServerUrl: 'http://localhost:28080/consent/confirmation/ais',
  profileServerUrl: 'http://localhost:48080/api/v1/aspsp-profile',
  keycloak: keycloakConfig,
  xRequestId: '2f77a125-aa7a-45c0-b414-cea25a116035',
  tppQwacCertificate: 'qwac certificate'
};
