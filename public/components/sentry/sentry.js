import { get as getConfig } from 'js/config';

/*
This is raven's loader example code but adapted for webpack chunks
https://github.com/getsentry/raven-js/blob/master/packages/raven-js/src/loader.js
*/
const onRavenLoaded = new Promise((done, err) => {
  /* eslint-disable */
  (function(_window, _document, _script, _onerror, _onunhandledrejection) {
    const SENTRY_SDK = _window.SENTRY_SDK;

    // Create a namespace and attach function that will store captured exception
    // Because functions are also objects, we can attach the queue itself straight to it and save some bytes
    var queue = function(exception) {
      queue.data.push(exception);
    };
    queue.data = [];

    // Store reference to the old `onerror` handler and override it with our own function
    // that will just push exceptions to the queue and call through old handler if we found one
    const _oldOnerror = _window[_onerror];
    _window[_onerror] = function(message, source, lineno, colno, exception) {
      // Use keys as "data type" to save some characters"
      queue({
        e: [].slice.call(arguments)
      });

      if (_oldOnerror) _oldOnerror.apply(_window, arguments);
    };

    // Do the same store/queue/call operations for `onunhandledrejection` event
    const _oldOnunhandledrejection = _window[_onunhandledrejection];
    _window[_onunhandledrejection] = function(exception) {
      queue({
        p: exception.reason
      });
      if (_oldOnunhandledrejection)
        _oldOnunhandledrejection.apply(_window, arguments);
    };

    // Once our SDK is loaded
    import('raven-js').then(Raven => {
      try {
        // Restore onerror/onunhandledrejection handlers
        _window[_onerror] = _oldOnerror;
        _window[_onunhandledrejection] = _oldOnunhandledrejection;

        const data = queue.data;
        const SDK = Raven;
        // Configure it using provided DSN and config object
        init(Raven);
        // Because we installed the SDK, at this point we have an access to TraceKit's handler,
        // which can take care of browser differences (eg. missing exception argument in onerror)
        const tracekitErrorHandler = _window[_onerror];

        // And capture all previously caught exceptions
        if (data.length) {
          for (let i = 0; i < data.length; i++) {
            if (data[i].e) {
              tracekitErrorHandler.apply(SDK.TraceKit, data[i].e);
            } else if (data[i].p) {
              SDK.captureException(data[i].p);
            }
          }
        }
        done(Raven);
      } catch (o_O) {
        err(o_O);
      }
    });
  })(window, document, 'script', 'onerror', 'onunhandledrejection');
});

const init = Raven => {
  const dsn = getConfig('sentryDsn');
  if (dsn) Raven.config(dsn).install();
  else console.warn('Sentry configuration not found');
};

const captureExceptionAsync = ex => {
  onRavenLoaded.then(Raven => {
    Raven.captureException(ex);
  });
};

const getRaven = () => onRavenLoaded;

export { getRaven, captureExceptionAsync };
