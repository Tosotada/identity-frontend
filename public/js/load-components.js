/* @flow */

import { components } from 'js/components';
import { getRaven } from 'components/sentry/sentry';

const ERR_MALFORMED_LOADER = 'Missing loader parts';

const initOnceList = [];

class Component {
  init: HTMLElement => Promise<void>;
  initOnce: ?() => Promise<void>;
  selector: string;

  constructor(componentArr: any[]) {
    if (typeof componentArr[0] !== 'function') {
      throw new Error([ERR_MALFORMED_LOADER, componentArr]);
    }
    if (typeof componentArr[1] !== 'string') {
      throw new Error([ERR_MALFORMED_LOADER, componentArr]);
    }
    this.init = componentArr[0];
    this.selector = componentArr[1];
    if (componentArr[2] && typeof componentArr[2] === 'function') {
      this.initOnce = componentArr[2];
    }
  }
}

const loadComponent = ($root: HTMLElement, component: Component): void => {
  try {
    [...$root.querySelectorAll(component.selector)].forEach($target => {
      if (component.initOnce && !initOnceList.includes(component.selector)) {
        component.initOnce();
        initOnceList.push(component.selector);
      }
      $target.dataset.enhanced = 'true';
      component.init($target);
    });
  } catch (err) {
    getRaven().then(Raven => {
      Raven.context(() => {
        Raven.captureBreadcrumb({
          message: 'Loading component',
          data: {
            component: component.selector
          }
        });
        Raven.captureException(err);
      });
    });
  }
};

const loadComponents = ($root: HTMLElement): void => {
  components.forEach(component => {
    loadComponent($root, new Component(component));
  });
};

export { loadComponents };
