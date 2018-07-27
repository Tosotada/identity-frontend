// @flow

/*
You can use this component to wrap any flow of n steps
in a nice single page app way, you can see how this works
in the sign in page or in the sign in token request page
but basically you want to wrap your flow in
`div.ajax-step-flow` and `div.ajax-step-flow__slide`

The parent one (this file) acts as a 'stage' of sorts while
the second one is the actual 'page' of the flow. They are
separated for clarity.

Wrap your steps in these divs and make sure their routes are
inside `_valid-routes.js` and you should be good to go!
*/

import {
  EV_DONE,
  getSlide
} from 'components/ajax-step-flow/ajax-step-flow__slide';
import { loadComponents } from 'js/load-components';
import { pageView } from '../analytics/ga';

const selector: string = '.ajax-step-flow';

const ERR_MALFORMED_EVENT: string = 'Something went wrong';
const ERR_MALFORMED_HTML: string = 'Something went wrong';

const STATE_INITIATOR: string = 'ajax-step-flow-state-init';

const pushSlide = (
  $old: HTMLElement,
  $new: HTMLElement,
  reverse: boolean = false
): Promise<HTMLElement> => {
  const classNames = reverse
    ? {
        in: 'ajax-step-flow__slide--in-reverse',
        out: 'ajax-step-flow__slide--out-reverse'
      }
    : {
        in: 'ajax-step-flow__slide--in',
        out: 'ajax-step-flow__slide--out'
      };

  const animateOut = () =>
    new Promise(resolve => {
      if ('AnimationEvent' in window) {
        $old.addEventListener('animationend', () => {
          $old.remove();
          resolve($new);
        });
        requestAnimationFrame(() => {
          $old.classList.remove('ajax-step-flow__slide--visible');
          $old.classList.add(classNames.out);
        });
        $new.addEventListener('animationend', () => {
          [
            'ajax-step-flow__slide--in',
            'ajax-step-flow__slide--out',
            'ajax-step-flow__slide--in-reverse',
            'ajax-step-flow__slide--out-reverse'
          ].forEach(_ => $new.classList.remove(_));
        });
        requestAnimationFrame(() => {
          ['ajax-step-flow__slide--visible', classNames.in].forEach(_ =>
            $new.classList.add(_)
          );
        });
      } else {
        $old.remove();
        resolve($new);
      }
    });

  const $oldParent = $old.parentNode;
  if ($oldParent) {
    $oldParent.appendChild($new);
  } else {
    throw new Error(ERR_MALFORMED_HTML);
  }
  return animateOut().then(
    $element =>
      new Promise(yay => {
        $element.tabIndex = -1;
        requestAnimationFrame(() => {
          $element.focus();
          yay($element);
        });
      })
  );
};

const initOnce = (): void => {
  window.addEventListener('popstate', ev => {
    if (
      ev.state &&
      ev.state.initiator &&
      ev.state.initiator === STATE_INITIATOR
    ) {
      ev.preventDefault();
      ev.stopPropagation();
      window.location.reload();
    }
  });
};

const onSlide = (
  $component: HTMLElement,
  $slide: HTMLElement,
  href: String,
  isInitial: boolean = false
): void => {
  /* push state */
  if (isInitial) {
    window.history.replaceState(
      {
        initiator: STATE_INITIATOR
      },
      '',
      href
    );
  } else {
    window.history.pushState(
      {
        initiator: STATE_INITIATOR
      },
      '',
      href
    );
  }

  /* tell GA about it */
  if (!isInitial) {
    pageView();
  }

  /* make sure the container looks ok during transitions */
  $component.style.minHeight = `${$slide.clientHeight * 1.1}px`;
};

const triggerAutoFocus = ($slide): void => {
  const $focusable = $slide.querySelector('[autofocus]');
  if ($focusable) {
    $focusable.focus();
  }
};

const init = ($component: HTMLElement): void => {
  onSlide($component, getSlide($component), window.location.href, true);
  $component.addEventListener(EV_DONE, (ev: mixed) => {
    if (ev instanceof CustomEvent) {
      const $slide = getSlide($component);
      const $new = ev.detail.$slide;
      const url = ev.detail.url;

      if (!$slide || !$new) {
        throw new Error(ERR_MALFORMED_HTML);
      }

      /* push the slide */
      pushSlide($slide, $new, ev.detail.reverse).then(() => {
        onSlide($component, $new, url);
        loadComponents((($new.parentElement: any): HTMLElement));
        triggerAutoFocus($new);
      });
    } else {
      throw new Error(ERR_MALFORMED_EVENT);
    }
  });
};

export { init, selector, initOnce };
