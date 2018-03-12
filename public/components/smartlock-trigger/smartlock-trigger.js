// @flow

import qs from 'qs';
import { route } from 'js/config';
import { customMetric } from 'components/analytics/ga';

const className: string = 'smartlock-trigger';

const ERR_WRONG_CREDENTIAL = 'Error fetching smart lock credentials';
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
  fetch(route('smartlockSignIn'), {
    credentials: 'same-origin',
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: qs.stringify({
      email: credentials.id,
      password: credentials.password,
      csrfToken: csrfToken
    })
  }).then(r => {
    if (r.status == 200) {
      customMetric({ name: 'SigninSuccessful', type: 'SmartLockSignin' });
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

  if (navigator && navigator.credentials) {

    navigator.credentials.preventSilentAccess();

    navigator.credentials
      .get({
        password: true
      })
      .then(c => {
        if (c instanceof PasswordCredential) {
          smartLockSignIn(c, returnUrl, csrfToken);
        } else {
          throw new Error(ERR_WRONG_CREDENTIAL);
        }
      }).catch(err => {
        throw err;
    })
  }
};

export { className, init };
