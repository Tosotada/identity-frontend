// @flow

const ERR_MISSING_KEY: string = 'Missing configuration part';
const ERR_MISSING_CONFIG: string = 'Missing #id_config';

const reduceReplacers = (text: string, ...replacers: string[]) =>
  replacers.reduce(
    (returnableText, replacer, key) =>
      returnableText.replace(`{${key}}`, replacer),
    text
  );

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
  if (config.routes && config.routes[routeToGet])
    return config.routes[routeToGet];
  throw new Error(ERR_MISSING_KEY);
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
