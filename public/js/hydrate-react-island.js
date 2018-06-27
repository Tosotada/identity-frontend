// @flow
import { h, render, Component } from 'preact';
import Raven from 'raven-js';

const getBootstrap = ($component: HTMLElement): {} => {
  const $bootstrap: ?HTMLElement = $component.querySelector(
    '.react-island__bootstrap'
  );
  if ($bootstrap && $bootstrap.innerText) {
    try {
      return JSON.parse($bootstrap.innerText);
    } catch (err) {
      Raven.captureException(err, JSON.stringify(err));
      return {};
    }
  } else {
    return {};
  }
};

const hydrate = ($component: HTMLElement, island: Component): void => {
  const bootstrap = getBootstrap($component);

  while ($component.firstChild) $component.removeChild($component.firstChild);

  render(h(island, bootstrap), $component);
};

export { hydrate };
