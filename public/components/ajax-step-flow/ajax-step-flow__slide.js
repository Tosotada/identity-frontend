// @flow

import { showErrorText } from 'components/form/form-feedback-wrap';
import { getUrlErrors } from 'js/get-url-errors';
import {
  MalformedResponseError,
  MalformedHtmlError,
  BackendError,
  ContextError
} from 'js/errors';
import { getRaven } from 'components/sentry/sentry';
import {
  formRoutes as validAjaxFormRoutes,
  linkRoutes as validAjaxLinkRoutes
} from './_valid-routes';

const selector: string = '.ajax-step-flow__slide';

const SLIDE_STATE_LOADING: string = 'SLIDE_STATE_LOADING';
const SLIDE_STATE_DEFAULT: string = 'SLIDE_STATE_DEFAULT';

const EV_DONE: string = 'form-done';

type ParsedResponse = {
  type: 'json' | 'html',
  body: {
    html?: string,
    returnUrl?: string
  },
  url: string
};

const getSlide = ($wrapper: HTMLElement): HTMLElement => {
  const $slide = $wrapper.querySelector(selector);
  if ($slide) return $slide;
  throw new MalformedHtmlError($wrapper.innerHTML);
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
): Promise<ParsedResponse> =>
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
        throw new MalformedResponseError(response);
      }
      if (errors.length) {
        throw new BackendError(errors);
      }
      return Promise.all([response, response.text()]);
    })
    .then(([response, text]) => {
      try {
        const json = JSON.parse(text);
        return {
          type: 'json',
          body: json,
          url: response.url
        };
      } catch (e) {
        return {
          type: 'html',
          body: {
            html: text
          },
          url: response.url
        };
      }
    });

const catchSlide = ($slide: HTMLElement, err: Error): void => {
  $slide.dataset.state = SLIDE_STATE_DEFAULT;
  if (err instanceof BackendError) {
    err.errors.forEach(showErrorText);
  } else {
    showErrorText('error-unexpected');
  }
  getRaven().then(Raven => {
    Raven.context(() => {
      Raven.captureBreadcrumb({
        message: 'Ajax step slide',
        data: err instanceof ContextError ? err.context : {}
      });
      Raven.captureException(err);
    });
  });
};

const fetchAndDispatchSlide = (
  action: string,
  $slide: HTMLElement,
  fetchProps: {},
  props: { reverse: boolean } = { reverse: false }
): Promise<void> =>
  fetchSlide(action, $slide, fetchProps)
    .then(parsedResponse => {
      if (parsedResponse.type === 'json' && parsedResponse.body.returnUrl) {
        window.location.href = parsedResponse.body.returnUrl;
        return new Promise(() => {});
      } else if (parsedResponse.body.html) {
        return dispatchDone($slide, {
          $slide: getSlideFromFetch(parsedResponse.body.html),
          url: parsedResponse.url,
          reverse: props.reverse
        });
      }

      throw new MalformedResponseError(parsedResponse);
    })
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
