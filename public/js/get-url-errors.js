// @flow
import { getRaven } from 'components/sentry/sentry';

const getUrlErrors = (url: string): string[] => {
  try {
    const parsedUrl = new URL(url);
    const errorParams = parsedUrl.searchParams.get('error');

    if (errorParams) {
      return errorParams.split(',');
    }
    return [];
  } catch (err) {
    getRaven().then(Raven => {
      Raven.captureException(err);
    });
    return [];
  }
};

export { getUrlErrors };
export default getUrlErrors;
