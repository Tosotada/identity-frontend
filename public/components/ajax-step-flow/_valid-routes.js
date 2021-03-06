import { route } from 'js/config';

type ValidRouteList = string[];

const formRoutes: ValidRouteList = [
  'twoStepSignInAction',
  'signInSecondStepCurrentAction',
  'sendResubLinkAction'
].map(_ => route(_, true));

const linkRoutes: ValidRouteList = [route('twoStepSignIn')];

export { formRoutes, linkRoutes };
