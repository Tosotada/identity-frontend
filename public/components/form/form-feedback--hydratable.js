// @flow

const selector: string = '.form-feedback--hydratable';

const init = ($component: Element): void => {
  $component.innerText = 'Hey im hydratable is that even a real english word';
};

export { selector, init };
