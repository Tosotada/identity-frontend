/* eslint-disable */

import { getElementById, sessionStorage } from '../browser/browser';

import { initPhoneField } from '../lib/phone-field';

import { fetchTracker } from '../analytics/ga';

const STORAGE_KEY = 'gu_id_register_state';

class RegisterFormFields {
  constructor(
    firstNameField,
    lastNameField,
    emailField,
    displayNameField,
    optionalCountryCode,
    optionalCountryIsoName,
    optionalPhoneNumber,
    optionalHideUsername
  ) {
    this.firstName = firstNameField;
    this.lastName = lastNameField;
    this.email = emailField;
    this.displayName = displayNameField;
    this.optionalCountryCode = optionalCountryCode;
    this.optionalCountryIsoName = optionalCountryIsoName;
    this.optionalPhoneNumber = optionalPhoneNumber;
    this.optionalHideDisplayName = optionalHideUsername;
  }

  setValues({
    firstName,
    lastName,
    email,
    displayName,
    optionalCountryCode,
    optionalCountryIsoName,
    optionalPhoneNumber
  } = {}) {
    this.firstName.setValue(firstName);
    this.lastName.setValue(lastName);
    // If we don't receive an email from the backend model use local storage
    if (this.email.value().length === 0) {
      this.email.setValue(email);
    }
    this.displayName.setValue(displayName);
    if (this.optionalPhoneNumber) {
      this.optionalPhoneNumber.setValue(optionalPhoneNumber);
    }
    if (this.optionalCountryCode) {
      this.optionalCountryCode.setValue(optionalCountryCode);
    }
    if (this.optionalCountryIsoName) {
      this.optionalCountryIsoName.setValue(optionalCountryIsoName);
    }
  }

  mapValues(callback) {
    let rt = Object.assign({}, this);
    for (let k in rt) {
      rt[k] = callback(rt[k]);
    }
    return rt;
  }

  toJSON() {
    return this.mapValues(field => (field ? field.value() : undefined));
  }
}

class RegisterFormModel {
  constructor(
    formElement,
    firstNameField,
    lastNameField,
    emailField,
    displayNameField,
    optionalCountryCode,
    optionalCountryIsoName,
    optionalPhoneNumber,
    optionalHideUsername,
    gaClientIdElement
  ) {
    this.formElement = formElement;
    this.gaClientIdElement = gaClientIdElement;

    this.fields = new RegisterFormFields(
      firstNameField,
      lastNameField,
      emailField,
      displayNameField,
      optionalCountryCode,
      optionalCountryIsoName,
      optionalPhoneNumber,
      optionalHideUsername
    );

    this.addBindings();
    this.saveClientId();
  }

  addBindings() {
    this.formElement.on('submit', this.formSubmitted.bind(this));
    this.fields.firstName.on('blur', this.updateDisplayName.bind(this));
    this.fields.lastName.on('blur', this.updateDisplayName.bind(this));
    this.fields.email.on('invalid', ev => {
      this.validateEmail(ev);
    });
  }

  validateEmail(event) {
    event.target.setCustomValidity('Please enter a valid email address');
  }

  updateDisplayName() {
    if (
      this.fields.optionalHideDisplayName &&
      this.fields.optionalHideDisplayName.value()
    ) {
      const displayName =
        this.fields.firstName.value() + ' ' + this.fields.lastName.value();
      this.fields.displayName.setValue(displayName);
    }
  }

  loadState() {
    this.state = RegisterFormState.fromStorage();
    this.fields.setValues(this.state);
  }

  saveState() {
    this.state = RegisterFormState.fromForm(this);
    this.state.save();
  }

  saveClientId() {
    fetchTracker(tracker => {
      // Save the GA client id to be passed with the form submission
      if (this.gaClientIdElement) {
        this.gaClientIdElement.setValue(tracker.get('clientId'));
      }
    });
  }

  formSubmitted() {
    this.saveState();
  }

  static fromDocument() {
    const form = getElementById('register_form');
    const firstNameField = getElementById('register_field_firstname');
    const lastNameField = getElementById('register_field_lastname');
    const emailField = getElementById('register_field_email');
    const displayNameField = getElementById('register_field_displayName');
    const optionalPhoneNumber = getElementById('register_field_localNumber');
    const optionalCountryCode = getElementById('register_field_countryCode');
    const optionalCountryIsoName = getElementById(
      'register_field_countryIsoName'
    );
    const optionalHideUsername = getElementById(
      'register_field_hideDisplayName'
    );
    const gaClientIdElement = getElementById('register_ga_client_id');

    if (
      form &&
      firstNameField &&
      lastNameField &&
      emailField &&
      displayNameField &&
      gaClientIdElement
    ) {
      return new RegisterFormModel(
        form,
        firstNameField,
        lastNameField,
        emailField,
        displayNameField,
        optionalCountryCode,
        optionalCountryIsoName,
        optionalPhoneNumber,
        optionalHideUsername,
        gaClientIdElement
      );
    }
  }
}

class RegisterFormState {
  constructor(
    firstName = '',
    lastName = '',
    email = '',
    displayName = '',
    optionalCountryCode = '',
    optionalCountryIsoName = '',
    optionalPhoneNumber = '',
    optionalHideDisplayName = false
  ) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.displayName = displayName;
    this.optionalCountryCode = optionalCountryCode;
    this.optionalCountryIsoName = optionalCountryIsoName;
    this.optionalPhoneNumber = optionalPhoneNumber;
    this.optionalHideDisplayName = optionalHideDisplayName;
  }

  save() {
    sessionStorage.setJSON(STORAGE_KEY, this);
  }

  /**
   * @return {RegisterFormState}
   */
  static fromObject({
    firstName,
    lastName,
    email,
    displayName,
    optionalCountryCode,
    optionalCountryIsoName,
    optionalPhoneNumber,
    optionalHideDisplayName
  } = {}) {
    return new RegisterFormState(
      firstName,
      lastName,
      email,
      displayName,
      optionalCountryCode,
      optionalCountryIsoName,
      optionalPhoneNumber,
      optionalHideDisplayName
    );
  }

  /**
   * @return {RegisterFormState}
   */
  static fromStorage() {
    const existingState = sessionStorage.getJSON(STORAGE_KEY);

    return RegisterFormState.fromObject(existingState);
  }

  /**
   * @param {RegisterFormModel} form
   * @return {RegisterFormState}
   */
  static fromForm(form) {
    return RegisterFormState.fromObject(form.fields.toJSON());
  }
}

export function init() {
  const form = RegisterFormModel.fromDocument();

  if (form) {
    checkForCredentials();
    form.loadState();

    if (form.fields.optionalPhoneNumber) {
      initPhoneField(
        form,
        form.fields.optionalCountryCode,
        form.fields.optionalCountryIsoName,
        form.fields.optionalPhoneNumber
      );
    }
  }

  return form;
}

function checkForCredentials() {
  if (navigator.credentials && navigator.credentials.preventSilentAccess) {
    navigator.credentials
      .get({
        password: true
      })
      .then(c => {
        if (c instanceof PasswordCredential) {
          const link = getElementById('sign_in_page_link');
          if (link && link.elem.href) {
            window.location.replace(link.elem.href);
          }
        }
      });
  }
}
