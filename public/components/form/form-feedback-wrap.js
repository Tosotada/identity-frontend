// @flow
import { localisedError } from 'js/config';

const $elements: HTMLElement[] = [];

const selector: string = '.form-feedback-wrap';

const errors: string[] = [];

const renderErrors = (): void => {
  $elements.forEach($element => {
    $element.innerHTML = '';
    errors.forEach(error => {
      const $div = document.createElement('div');
      $div.innerHTML = error;
      const childClassName = $element.dataset.appendClassname;
      if (childClassName) $div.className = childClassName;
      $element.appendChild($div);
    });
  });
};

const showError = (error: string): void => {
  if ($elements.length < 1) {
    alert(error); /* eslint-disable-line no-alert */
  } else {
    errors.push(error);
    renderErrors();
  }
};

const showErrorText = (error: string): void => {
  showError(localisedError(error));
};

const init = ($element: HTMLElement): Promise<void> => {
  $elements.push($element);
  errors.length = 0;
  return Promise.resolve();
};

export { selector, init, showError, showErrorText };
