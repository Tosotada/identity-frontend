import 'main.css';
import './components/sentry/sentry';

import { components } from './js/components';
import { loadComponents } from './js/load-components';
import { logPageView } from './components/analytics/analytics';

logPageView();
loadComponents(document, components);
