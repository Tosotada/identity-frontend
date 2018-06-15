/* @flow */

import Raven from 'raven-js';

import {
  init as initFormFieldWrapPhone,
  selector as selectorFormFieldWrapPhone
} from 'components/form/form-field-wrap--phone';
import {
  init as initAjaxForm,
  selector as selectorAjaxForm,
  initOnce as initOnceAjaxForm
} from 'components/ajax-step-flow/ajax-step-flow';
import {
  init as initAjaxFormSlide,
  selector as selectorAjaxFormSlide
} from 'components/ajax-step-flow/ajax-step-flow__slide';
import {
  init as initSmartLock,
  selector as selectorSmartLock
} from 'components/smartlock-trigger/smartlock-trigger';
import {
  init as initFormErrorWrap,
  selector as selectorErrorWrap
} from 'components/form-feedback-wrap/index';
import {
  init as initGAClientIdFormField,
  selector as gaClientIdSelector
} from 'components/analytics/bind-ga-client-id';
import {
  init as initFormInput,
  selector as selectorFormInput
} from 'components/form/form-input';
import {
  init as initOauthCta,
  selector as selectorOauthCta
} from 'components/oauth-cta/oauth-cta';
import {
  init as initInPageClick,
  selector as selectorInPageClick
} from 'components/analytics/analytics-in-page-click';

const ERR_MALFORMED_LOADER = 'Missing loader parts';
const ERR_COMPONENT_THROW = 'Uncaught component error';

const components: any[] = [
  [initFormInput, selectorFormInput],
  [initAjaxForm, selectorAjaxForm, initOnceAjaxForm],
  [initAjaxFormSlide, selectorAjaxFormSlide],
  [initSmartLock, selectorSmartLock],
  [initInPageClick, selectorInPageClick],
  [initFormErrorWrap, selectorErrorWrap],
  [initOauthCta, selectorOauthCta],
  [initGAClientIdFormField, gaClientIdSelector],
  [initFormFieldWrapPhone, selectorFormFieldWrapPhone]
];

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
    Raven.captureException(err, [ERR_COMPONENT_THROW, component.selector]);
    console.error(err, [ERR_COMPONENT_THROW, component.selector]);
  }
};

const loadComponents = ($root: HTMLElement): void => {
  components.forEach(component => {
    loadComponent($root, new Component(component));
  });
};

export { loadComponents };
export default loadComponents;
