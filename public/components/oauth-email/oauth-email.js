// @flow

const ERR_MALFORMED_HTML = 'Malformed html';

const selector: string = '.oauth-email';

const init = ($component: HTMLElement): void => {
  const $button: ?HTMLElement = $component.querySelector('.oauth-email__cta');
  const $wrap: ?HTMLElement = $component.querySelector('.oauth-email__wrap');

  if (!$button || !$wrap) {
    throw new Error(ERR_MALFORMED_HTML);
  }

  const $focusable: ?HTMLElement = $wrap.querySelector('input[autofocus]');

  $wrap.style.display = 'none';

  $button.addEventListener('click', (ev: Event) => {
    ev.preventDefault();
    $wrap.style.display = 'block';
    if ($focusable) $focusable.focus();
  });
};

export { init, selector };
