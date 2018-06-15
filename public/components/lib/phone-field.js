/* eslint-disable */

const PLUGIN_OPTIONS = {
  initialCountry: 'gb',
  preferredCountries: ['gb', 'us', 'au']
};

export function initPhoneField(
  form,
  countryCodeElement,
  countryIsoName,
  localNumberElement
) {
  Promise.all([
    import('jquery'),
    import('raw-loader!intl-tel-input/build/css/intlTelInput.css'),
    import('intl-tel'),
    import('intl-tel-utils')
  ]).then(([jq, css]) => {
    const $ = jq.default;

    const $style = document.createElement('style');
    $style.innerText = css.default + '.intl-tel-input{display:block;}';
    document.body.appendChild($style);

    initializeFields(
      $(form),
      $(countryCodeElement),
      $(countryIsoName),
      $(localNumberElement)
    );
  });
}

function initializeFields(form, countryCode, countryIsoName, localNumber) {
  // The core view has a select and an input field. When JS is enabled and running
  // hide the select and replace it with a jQuery phone number plugin
  const selectedCountry = countryIsoName.val();
  if (selectedCountry) {
    PLUGIN_OPTIONS.initialCountry = selectedCountry;
  }
  localNumber.intlTelInput(PLUGIN_OPTIONS);
  countryCode.parent().hide();
  localNumber
    .parents('.register-form__control-column--local-number')
    .removeClass('register-form__control-column--local-number')
    .addClass('register-form__control-column--local-number--wide');

  // The form is persisted in local storage on submit, but because we're loaded asynchronously
  // persistence is done before synchronization, to account for that, update the fields on change
  form.on('change', updateHiddenField);
  form.on('submit', updateHiddenField);
  localNumber.on('countrychange', updateHiddenField);

  function updateHiddenField() {
    const { iso2, dialCode } = localNumber.intlTelInput(
      'getSelectedCountryData'
    );
    countryCode.val(dialCode);
    localNumber.val(
      localNumber
        .intlTelInput('getNumber')
        .replace(new RegExp('^\\+' + dialCode), '')
    );
    countryIsoName.val(iso2);
  }
}
