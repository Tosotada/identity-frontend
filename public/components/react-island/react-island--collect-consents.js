// @flow
const selector: string = '.react-island--collect-consents';

const init = ($component: HTMLElement): void => {
  Promise.all([
    import('react-elements/CollectConsents'),
    import('js/hydrate-react-island')
  ]).then(([{ CollectConsents }, { hydrate }]) => {
    hydrate($component, CollectConsents);
  });
};

export { selector, init };
