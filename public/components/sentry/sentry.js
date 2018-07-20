import { get as getConfig } from 'js/config';

/*
This is raven's loader example code but adapted for webpack chunks
https://github.com/getsentry/raven-js/blob/master/packages/raven-js/src/loader.js
*/
const onRavenLoaded: Promise<{}> = new Promise((done, err) => {
  /* eslint-disable */

  // Create a namespace and attach function that will store captured exception
  // Because functions are also objects, we can attach the queue itself straight to it and save some bytes
  var queue = function(exception) {
    queue.data.push(exception);
  };
  queue.data = [];

  // Store reference to the old `onerror` handler and override it with our own function
  // that will just push exceptions to the queue and call through old handler if we found one
  const _oldOnerror = window['onerror'];
  window['onerror'] = function(message, source, lineno, colno, exception) {
    // Use keys as "data type" to save some characters"
    queue({
      e: [].slice.call(arguments)
    });

    if (_oldOnerror) _oldOnerror.apply(window, arguments);
  };

  // Do the same store/queue/call operations for `onunhandledrejection` event
  const _oldOnunhandledrejection = window['onunhandledrejection'];
  window['onunhandledrejection'] = function(exception) {
    queue({
      p: exception.reason
    });
    if (_oldOnunhandledrejection)
      _oldOnunhandledrejection.apply(window, arguments);
  };

  // Once our SDK is loaded
  import('raven-js')
    .then(_ => _.default)
    .then(Raven => {
      try {
        // Restore onerror/onunhandledrejection handlers
        window['onerror'] = _oldOnerror;
        window['onunhandledrejection'] = _oldOnunhandledrejection;

        const data = queue.data;
        // Configure it using provided DSN and config object
        const dsn = getConfig('sentryDsn');
        const version = getConfig('appVersion');
        if (dsn)
          Raven.config(dsn, {
            version
          }).install();
        else console.warn('Sentry configuration not found');

        // Because we installed the SDK, at this point we have an access to TraceKit's handler,
        // which can take care of browser differences (eg. missing exception argument in onerror)
        const tracekitErrorHandler = window['onerror'];

        // And capture all previously caught exceptions
        if (data.length) {
          for (let i = 0; i < data.length; i++) {
            if (data[i].e) {
              tracekitErrorHandler.apply(Raven.TraceKit, data[i].e);
            } else if (data[i].p) {
              Raven.captureException(data[i].p);
            }
          }
        }
        done(Raven);
      } catch (o_O) {
        err(o_O);
      }
    });
});

const getRaven = (): Promise<{}> => {
  return onRavenLoaded;
};

export { getRaven };
