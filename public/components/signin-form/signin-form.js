/* eslint-disable */

/*global window, document*/

import { getElementById, sessionStorage } from '../browser/browser';
import { customMetric, fetchTracker } from '../analytics/ga';

const STORAGE_KEY = 'gu_id_signIn_state';
const SMART_LOCK_STORAGE_KEY = 'gu_id_smartLock_state';

class SignInFormModel {
  constructor(formElement, emailField, passwordField, gaClientIdElement) {
    this.formElement = formElement;
    this.emailFieldElement = emailField;
    this.passwordFieldElement = passwordField;
    this.gaClientIdElement = gaClientIdElement;
    this.addBindings();
    this.saveClientId();
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
    }
  }

  storeRedirect(c) {
    navigator.credentials.store(c).then(_ => {
      window.location = getElementById('signin_returnUrl').value();
    });
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
