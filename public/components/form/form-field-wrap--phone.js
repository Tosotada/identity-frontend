// @flow
import { initPhoneField } from '../lib/phone-field';

const selector: string = '.form-field-wrap--phone';

const init = ($component: Element): void => {
  initPhoneField(
    $component.closest('form'),
    $component.querySelector('#register_field_countryCode'),
    $component.querySelector('#register_field_countryIsoName'),
    $component.querySelector('#register_field_localNumber')
  );
};

export { selector, init };
