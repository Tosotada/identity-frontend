// @flow

import React, { Component } from 'react';

type Props = {
  title: string
};

class Header extends Component<Props> {
  render() {
    return (
      <header className="layout-header">
        <h1 className="layout-header__title">{this.props.title}</h1>
      </header>
    );
  }
}

export { Header };
