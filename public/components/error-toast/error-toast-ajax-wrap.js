// @flow
import { localisedError } from 'js/config';

const $elements: HTMLElement[] = [];

const selector: string = '.error-toast-ajax-wrap';

let errors: string[] = [];

const renderErrors = (): void => {
  $elements.forEach(($element, index) => {
    $element.innerHTML = '';
    errors.forEach(error => {
      const $div = document.createElement('div');
      const $message = document.createElement('div');
      const $closeButton = document.createElement('button');
      $div.classList.add('error-toast');
      $message.classList.add('error-toast__msg');
      $closeButton.classList.add('error-toast__icon');
      $closeButton.innerHTML = '<span class="u-h">Close</span>';
      $message.innerText = error;
      $div.appendChild($message);
      $div.appendChild($closeButton);
      const childClassName = $element.dataset.appendClassname;
      if (childClassName) $div.className = childClassName;
      $element.appendChild($div);
      $closeButton.addEventListener('click', (event: Event) => {
        event.preventDefault();
        errors = errors.filter((_, i) => i !== index);
        renderErrors();
      });
    });
  });
};

const showError = (error: string): void => {
  if ($elements.length < 1) {
    alert(error); /* eslint-disable-line no-alert */
  } else {
    const haveSeenSameError: boolean = !!errors.find(elem => elem === error);
    if (!haveSeenSameError) {
      errors.push(error);
      renderErrors();
    }
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
