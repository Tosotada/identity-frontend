// @flow

import { fetchTracker } from './ga';

const selector: string = '.ga-client-id';

const init = ($component: HTMLInputElement): void => {
  fetchTracker(tracker => {
    $component.value = tracker.get('clientId');
  });
};

export { init, selector };
