// @flow

import { localisedError, route, text } from 'js/config';

const selector: string = '.form-feedback--hydratable';

const hydratableIds = {
  'register-error-email-conflict': {
    replacers: [
      `<a data-link-name="register-dupe : sign-in" href="${route(
        'signIn'
      )}">${text('actions.signIn')}</a>`,
      `<a data-link-name="register-dupe : reset" href="${route(
        'reset'
      )}">${text('actions.reset')}</a>`
    ]
  }
};

const init = ($component: HTMLElement): void => {
  const id: ?string = $component.dataset.feedbackId;
  if (id && hydratableIds[id]) {
    $component.innerHTML = localisedError(
      `${id}-hydrated`,
      ...hydratableIds[id].replacers
    );
    if (hydratableIds[id].callback) {
      hydratableIds[id].callback($component);
    }
  }
};

export { selector, init };