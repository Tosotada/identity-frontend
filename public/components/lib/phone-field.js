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
  ]).then(([jq,css])=>{

    const $ = jq.default;

    const $style = document.createElement('style');
    $style.innerText = css.default;
    document.body.appendChild($style);

    const tooltipElement = form.formElement.elem.querySelector(
      '.register-form__tooltip--phone-number'
    );
    const formElement = form.formElement.elem;
    initializeFields(
      $(formElement),
      $(countryCodeElement.elem),
      $(countryIsoName.elem),
      $(localNumberElement.elem)
    );
    initialiseTooltip($, tooltipElement, formElement);
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

function initialiseTooltip($, tooltip, formElement) {
  tooltip.setAttribute('hidden', '');
  $(tooltip).removeClass('register-form__tooltip--phone-number--nojs');
  $('.register-form__link--why-phone-number', formElement)
    .removeAttr('hidden')
    .on('click', toggleDropdown);
  $('.register-form__tooltip--phone-number__close', formElement).on(
    'click',
    toggleDropdown
  );

  function toggleDropdown() {
    if (tooltip.hasAttribute('hidden')) {
      tooltip.removeAttribute('hidden');
    } else {
      tooltip.setAttribute('hidden', '');
    }
  }
}
