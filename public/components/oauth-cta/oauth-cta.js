// @flow

import { fetchTracker } from '../analytics/ga';

const className: string = 'oauth-cta';

const init = ($component: HTMLAnchorElement): void => {
  fetchTracker(tracker => {
    const [clientId, connector] = [
      encodeURI(tracker.get('clientId')),
      $component.search.length ? '&' : '?'
    ];
    $component.href = `${$component.href}${connector}gaClientId=${clientId}`;
  });
};

export { init, className };
