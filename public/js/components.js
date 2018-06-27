/* @flow */

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
} from 'components/form/form-feedback-wrap';
import {
  init as initFormFeedbackHydratable,
  selector as selectorFormFeedbackHydratable
} from 'components/form/form-feedback--hydratable';
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
import {
  init as initReactIslandCc,
  selector as selectorReactIslandCc
} from 'components/react-island/react-island--collect-consents';

export const components: any[] = [
  [initFormInput, selectorFormInput],
  [initAjaxForm, selectorAjaxForm, initOnceAjaxForm],
  [initAjaxFormSlide, selectorAjaxFormSlide],
  [initSmartLock, selectorSmartLock],
  [initInPageClick, selectorInPageClick],
  [initFormErrorWrap, selectorErrorWrap],
  [initOauthCta, selectorOauthCta],
  [initGAClientIdFormField, gaClientIdSelector],
  [initFormFieldWrapPhone, selectorFormFieldWrapPhone],
  [initReactIslandCc, selectorReactIslandCc],
  [initFormFeedbackHydratable, selectorFormFeedbackHydratable]
];
