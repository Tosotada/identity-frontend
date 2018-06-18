// @flow

const ERR_MISSING_KEY: string = 'Missing configuration part';
const ERR_MISSING_CONFIG: string = 'Missing #id_config';

const searchParams = ['skipConfirmation', 'returnUrl', 'clientId'];

const reduceReplacers = (text: string, ...replacers: string[]): string =>
  replacers.reduce(
    (returnableText, replacer, key) =>
      returnableText.replace(`{${key}}`, replacer),
    text
  );

const getSearchParams = (urlRaw: string): ?string => {
  try {
    const url: URL = new URL(urlRaw);
    const string = searchParams
      .map(param => [param, url.searchParams.get(param)])
      .filter(param => param[1])
      .map(param => param.map(encodeURIComponent).join('='))
      .join('&');

    if (string.length > 0) {
      return string;
    }

    throw new Error(`No params in url "${urlRaw}"`);
  } catch (e) {
    return null;
  }
};

const config = (() => {
  try {
    const $idConfig = document.getElementById('id_config');
    if (!$idConfig) throw new Error(ERR_MISSING_CONFIG);
    const configText = $idConfig.innerHTML;
    return JSON.parse(configText);
  } catch (err) {
    console.error(err);
    return {};
  }
})();

const get = (key: string): any => {
  if (config[key]) return config[key];
  throw new Error(ERR_MISSING_KEY);
};

const route = (routeToGet: string): string => {
  const params = getSearchParams(window.location.href);
  if (config.routes && config.routes[routeToGet])
    return params
      ? `${config.routes[routeToGet]}?${params}`
      : config.routes[routeToGet];
  throw new Error([ERR_MISSING_KEY, routeToGet].join());
};

const text = (textKey: string, ...replacers: string[]): string => {
  if (config.text && config.text[textKey])
    return reduceReplacers(config.text[textKey], ...replacers);
  throw new Error([ERR_MISSING_KEY, textKey].join());
};

const localisedError = (
  localisedErrorToGet: string,
  ...replacers: string[]
): string => {
  if (config.localisedErrors && config.localisedErrors[localisedErrorToGet])
    return reduceReplacers(
      config.localisedErrors[localisedErrorToGet],
      ...replacers
    );
  throw new Error([ERR_MISSING_KEY, localisedErrorToGet].join());
};

export { get, route, localisedError, text };
