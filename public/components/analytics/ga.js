/* eslint-disable */

/**
 * GA Tracking
 */
import { configuration } from '../configuration/configuration';

const gaTracker = 'IdentityPropertyTracker';

export function init() {
  const gaUID = configuration.gaUID;
  return record(gaUID);
}

export function customMetric(event) {
  ga(gaTracker + '.send', 'event', buildGoogleAnalyticsEvent(event));
}

export function fetchTracker(callback) {
  ga(function() {
    const tracker = ga.getByName(gaTracker);
    return callback(tracker);
  });
}

export function pageView(path = location.pathname) {
  ga(gaTracker + '.send', 'pageview', location.pathname);
}

function record(gaUID) {
  loadGA();
  ga('create', gaUID, 'auto', gaTracker);
  ga(gaTracker + '.send', 'pageview');
}

function buildGoogleAnalyticsEvent(event) {
  return {
    eventCategory: 'identity',
    eventAction: event.name,
    eventLabel: event.type,
    dimension3: 'profile.theguardian.com',
    dimension4: navigator.userAgent,
    dimension5: window.location.href,
    forceSSL: true
  };
}

function loadGA() {
  (function(i, s, o, g, r, a, m) {
    i['GoogleAnalyticsObject'] = r;
    (i[r] =
      i[r] ||
      function() {
        (i[r].q = i[r].q || []).push(arguments);
      }),
      (i[r].l = 1 * new Date());
    (a = s.createElement(o)), (m = s.getElementsByTagName(o)[0]);
    a.async = 1;
    a.src = g;
    m.parentNode.insertBefore(a, m);
  })(
    window,
    document,
    'script',
    'https://www.google-analytics.com/analytics.js',
    'ga'
  );
}
