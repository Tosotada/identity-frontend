import 'main.css';
import './components/sentry/sentry';

import { loadComponents } from './js/load-components';
import { logPageView } from './components/analytics/analytics';

logPageView();
loadComponents(document);
import('components/fonts/_fonts.css');
