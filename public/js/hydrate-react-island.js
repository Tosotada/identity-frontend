// @flow
import { h, render } from 'preact';
import Raven from 'raven-js';

const getBootstrap = ($component: HTMLElement): {} => {
  const $bootstrap: ?HTMLScriptElement = $component.querySelector('.react-island__bootstrap');
  if($bootstrap) {
    try {
      return JSON.parse($bootstrap.innerText);
    }
    catch (err) {
      Raven.captureException(err, JSON.stringify(err));
      return {};
    }
  }
  else {
    return {};
  }
};

const hydrate = ($component: HTMLElement, island: Component): void => {

  const bootstrap = getBootstrap($component);

  while ($component.firstChild) $component.removeChild($component.firstChild);

  render(
    h(island, bootstrap), $component
  );
};

export { hydrate };
