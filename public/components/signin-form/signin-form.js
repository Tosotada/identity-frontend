/* eslint-disable */

/*global window, document*/

import { getElementById, sessionStorage } from '../browser/browser';
import { customMetric, fetchTracker } from '../analytics/ga';
import { init as initOAuthBindings } from '../oauth-cta/_oauth-cta.js';

const STORAGE_KEY = 'gu_id_signIn_state';
const SMART_LOCK_STORAGE_KEY = 'gu_id_smartLock_state';

class SignInFormModel {
  constructor(formElement, emailField, passwordField, gaClientIdElement) {
    this.formElement = formElement;
    this.emailFieldElement = emailField;
    this.passwordFieldElement = passwordField;
    this.gaClientIdElement = gaClientIdElement;
    this.addBindings();
    this.smartLock();
    this.saveClientId();
    initOAuthBindings();
  }

  addBindings() {
    this.formElement.on('submit', this.formSubmitted.bind(this));
  }

  loadState() {
    this.state = SignInFormState.fromStorage();
    this.smartLockStatus = SmartLockState.fromStorage();
    // If we don't receive an email from the backend model use local storage
    if (this.emailFieldElement.value().length === 0) {
      this.emailFieldElement.setValue(this.state.email);
    }
  }

  saveState() {
    const email = this.emailFieldElement.value();

    this.state = new SignInFormState(email);
    this.state.save(email);
  }

  smartLockSetupOnSubmit() {
    if (navigator.credentials && navigator.credentials.preventSilentAccess) {
      const c = new PasswordCredential({
        id: this.emailFieldElement.value(),
        password: this.passwordFieldElement.value()
      });
      this.updateSmartLockStatus(true);
      this.smartLockSignIn(c);
    }
  }

  smartLock() {
    if (navigator.credentials && navigator.credentials.preventSilentAccess) {
      navigator.credentials
        .get({
          password: true
        })
        .then(c => {
          if (c instanceof PasswordCredential) {
            this.smartLockSignIn(c);
          }
        });
    }
  }

  storeRedirect(c) {
    navigator.credentials.store(c).then(_ => {
      window.location = getElementById('signin_returnUrl').value();
    });
  }

  smartLockSignIn(c) {
    if (this.smartLockStatus.status) {
      const form = new FormData(document.querySelector('#signin_form'));
      form.set('email', c.id);
      form.set('password', c.password);

      fetch('/actions/signin/smartlock', {
        credentials: 'same-origin',
        method: 'POST',
        body: form
      }).then(r => {
        if (r.status == 200) {
          this.updateSmartLockStatus(true);
          customMetric({ name: 'SigninSuccessful', type: 'SmartLockSignin' });
          this.storeRedirect(c);
        } else {
          r.json().then(j => {
            this.updateSmartLockStatus(false);
            window.location = j.url;
          });
        }
      });
    }
  }

  updateSmartLockStatus(status) {
    this.smartLockStatus = new SmartLockState(status);
    this.smartLockStatus.save();
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
    this.smartLockSetupOnSubmit();
    this.saveState();
  }

  static fromDocument() {
    const form = getElementById('signin_form');
    const emailField = getElementById('signin_field_email');
    const passwordField = getElementById('signin_field_password');
    const gaClientIdField = getElementById('signin_ga_client_id');

    if (form && emailField) {
      return new SignInFormModel(
        form,
        emailField,
        passwordField,
        gaClientIdField
      );
    }
  }
}

class SignInFormState {
  constructor(email = '') {
    this.email = email;
  }

  save() {
    sessionStorage.setJSON(STORAGE_KEY, this);
  }

  /**
   * @return {SignInFormState}
   */
  static fromObject({ email } = {}) {
    return new SignInFormState(email);
  }

  static fromStorage() {
    const existingState = sessionStorage.getJSON(STORAGE_KEY);

    return SignInFormState.fromObject(existingState);
  }
}

class SmartLockState {
  constructor(status = true) {
    this.status = status;
  }

  save() {
    sessionStorage.setJSON(SMART_LOCK_STORAGE_KEY, this);
  }

  /**
   * @return {SmartLockState}
   */
  static fromObject({ status } = {}) {
    return new SmartLockState(status);
  }

  static fromStorage() {
    const existingState = sessionStorage.getJSON(SMART_LOCK_STORAGE_KEY);

    return SmartLockState.fromObject(existingState);
  }
}

export function init() {
  const form = SignInFormModel.fromDocument();

  if (form) {
    form.loadState();
  }
}
