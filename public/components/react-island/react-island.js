// @flow
import { h, render } from 'preact';
import Raven from 'raven-js';


const selector: string = '.react-island';

const getBootstrap = ($component: HTMLElement): {} => {
    const $bootstrap: ?HTMLScriptElement = $component.querySelector('.react-island__bootstrap');
    if($bootstrap) {
      try {
        return JSON.parse($bootstrap.innerText);
      }
      catch (err) {
        Raven.captureException(err, JSON.stringify(err));
      }
    }
    else {
      return {};
    }
}

const init = ($component: HTMLElement): void => {

  import(`elements/${$component.dataset.element}`).then(element => {
    const bootstrap = getBootstrap($component);
    render(
      h(element.default, bootstrap), $component
    );
  })

};

export { selector, init };
