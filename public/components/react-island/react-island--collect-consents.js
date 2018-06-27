// @flow
import { CollectConsents } from 'elements/CollectConsents';
import {hydrate} from 'js/hydrate-react-island';

const selector: string = '.react-island--collect-consents';

const init = ($component: HTMLElement): void => {
  hydrate($component, CollectConsents);
};

export { selector, init };
