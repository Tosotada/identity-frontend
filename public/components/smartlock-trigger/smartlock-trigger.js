// @flow

import qs from 'qs';
import { route } from 'js/config';
import { fetchTracker } from 'components/analytics/ga';

const selector: string = '.smartlock-trigger';

const ERR_FAILED_SIGNIN = 'Error signing in with smart lock';
const ERR_MISSING_PARAMS = 'Missing parameters';

type Credential = {
  id: string,
  password: string
};

type PasswordCredential = Credential;

const smartLockSignIn = (
  credentials: Credential,
  returnUrl: string,
  csrfToken: string
) => {
  new Promise(fetchTracker)
    .then(tracker =>
      fetch(route('smartlockSignIn'), {
        credentials: 'same-origin',
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: qs.stringify({
          email: credentials.id,
          password: credentials.password,
          gaClientId: tracker.get('clientId'),
          csrfToken
        })
      })
    )
    .then(r => {
      if (r.status === 200) {
        window.location.href = returnUrl;
      } else {
        throw new Error(ERR_FAILED_SIGNIN);
      }
    });
};

const init = ($element: HTMLElement): void => {
  const [returnUrl, csrfToken] = [
    $element.dataset.returnUrl,
    $element.dataset.csrfToken
  ];

  if (!returnUrl || !csrfToken) {
    throw new Error(ERR_MISSING_PARAMS);
  }

  // $FlowFixMe
  if (navigator && navigator.credentials !== null) {
    const credentialsContainer = (navigator: any).credentials;

    credentialsContainer
      .get({
        password: true
      })
      .then(c => {
        // $FlowFixMe
        if (c instanceof PasswordCredential) {
          smartLockSignIn(c, returnUrl, csrfToken);
        }
      });
  }
};

export { selector, init };
