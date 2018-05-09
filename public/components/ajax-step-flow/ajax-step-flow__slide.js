// @flow

import { showErrorText } from '../form-feedback-wrap/index';
import { getUrlErrors } from '../../js/get-url-errors';
import {
  formRoutes as validAjaxFormRoutes,
  linkRoutes as validAjaxLinkRoutes
} from './_valid-routes';

const selector: string = '.ajax-step-flow__slide';

const SLIDE_STATE_LOADING: string = 'SLIDE_STATE_LOADING';
const SLIDE_STATE_DEFAULT: string = 'SLIDE_STATE_DEFAULT';

const EV_DONE: string = 'form-done';

const ERR_MALFORMED_HTML: string = 'ERR_MALFORMED_HTML';
const ERR_MALFORMED_RESPONSE: string = 'ERR_MALFORMED_RESPONSE';
const ERR_BACKEND_ERROR: string = 'ERR_BACKEND_ERROR';

const getSlide = ($wrapper: HTMLElement): HTMLElement => {
  const $slide = $wrapper.querySelector(selector);
  if ($slide) return $slide;
  throw new Error([ERR_MALFORMED_HTML, $slide]);
};

const getSlideFromFetch = (textHtml: string): HTMLElement => {
  const $wrapper: HTMLElement = document.createElement('div');
  $wrapper.innerHTML = textHtml;

  return getSlide($wrapper);
};

const dispatchDone = (
  $parent,
  {
    $slide,
    url,
    reverse = false
  }: { $slide: HTMLElement, url: string, reverse?: boolean }
): void => {
  const event = new CustomEvent(EV_DONE, {
    bubbles: true,
    detail: {
      $slide,
      url,
      reverse
    }
  });
  $parent.dispatchEvent(event);
};

const fetchSlide = (
  action: string,
  $slide: HTMLElement,
  fetchProps: {}
): Promise<string[]> =>
  Promise.resolve()
    .then(() => {
      $slide.dataset.state = SLIDE_STATE_LOADING;
    })
    .then(() =>
      window.fetch(
        action,
        Object.assign(
          {},
          {
            credentials: 'include',
            headers: {
              'x-gu-browser-rq': 'true'
            },
            redirect: 'follow',
            method: 'POST'
          },
          fetchProps
        )
      )
    )
    .then(response => {
      const errors = getUrlErrors(response.url);
      if (response.status !== 200) {
        throw new Error([ERR_MALFORMED_RESPONSE, response]);
      }
      if (errors.length) {
        throw new Error([ERR_BACKEND_ERROR, ...errors]);
      }
      return response.text().then(text => {
        try {
          const json = JSON.parse(text);
          if (json.returnUrl) {
            window.location.href = json.returnUrl;
            return new Promise(() => {});
          }
          throw new Error([ERR_MALFORMED_RESPONSE, response]);
        } catch (e) {
          return [text, response.url];
        }
      });
    });

const catchSlide = ($slide: HTMLElement, err: Error): void => {
  $slide.dataset.state = SLIDE_STATE_DEFAULT;
  if (err.message.split(',')[0] === ERR_BACKEND_ERROR) {
    err.message
      .split(',')
      .splice(1)
      .forEach(showErrorText);
  } else {
    showErrorText('error-unexpected');
  }
  console.error(err);
};

const fetchAndDispatchSlide = (
  action: string,
  $slide: HTMLElement,
  fetchProps: {},
  props: { reverse: boolean } = { reverse: false }
): Promise<void> =>
  fetchSlide(action, $slide, fetchProps)
    .then(([responseHtml, url]) =>
      dispatchDone($slide, {
        $slide: getSlideFromFetch(responseHtml),
        url,
        reverse: props.reverse
      })
    )
    .catch(err => catchSlide($slide, err));

const init = ($slide: HTMLElement): void => {
  const $links: HTMLAnchorElement[] = [
    ...($slide.querySelectorAll(`a.ajax-step-flow__link`): any)
  ]
    .filter(_ => _ instanceof HTMLAnchorElement)
    .filter(_ =>
      validAjaxLinkRoutes.map(r => _.href.includes(r)).some(c => c === true)
    );

  const $forms: HTMLFormElement[] = [...($slide.querySelectorAll(`form`): any)]
    .filter(_ => _ instanceof HTMLFormElement)
    .filter(_ => validAjaxFormRoutes.some(r => _.action.includes(r)));

  $forms.forEach(($form: HTMLFormElement) => {
    $form.addEventListener('submit', (ev: Event) => {
      ev.preventDefault();
      fetchAndDispatchSlide($form.action, $slide, {
        method: 'post',
        body: new FormData($form)
      });
    });
  });

  $links.forEach(($link: HTMLAnchorElement) => {
    $link.addEventListener('click', (ev: Event) => {
      ev.preventDefault();
      fetchAndDispatchSlide(
        $link.href,
        $slide,
        {
          method: 'get'
        },
        {
          reverse: $link.dataset.isReverse !== null
        }
      );
    });
  });
};

export { init, selector, EV_DONE, getSlide, getSlideFromFetch };
