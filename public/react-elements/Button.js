/* eslint react/no-danger: off */

import React, { Component } from 'react';
import arrow from 'components/form/arrow-right.svg';
import css from 'components/form/_form-button.css';

type Props = {
  href: ?string,
  children: Component[]
};

class Button extends Component<Props> {
  render() {
    return this.props.href ? (
      <a
        href={this.props.href}
        className={[css['form-button'], css['form-button--main']].join(' ')}
      >
        {this.props.children}
        <span dangerouslySetInnerHTML={{ __html: arrow }} />
      </a>
    ) : (
      <button className="form-button form-button--main">
        {this.props.children}
        <span dangerouslySetInnerHTML={{ __html: arrow }} />
      </button>
    );
  }
}

export { Button };
