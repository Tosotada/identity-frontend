import './components/sentry/sentry';
import './js/load-global-css';

import { loadComponents } from './js/load-components';
import { logPageView } from './components/analytics/analytics';

logPageView();
loadComponents(document);
