// @flow

import React, { Component } from 'react';
import { Button } from 'react-elements/Button';
import { Header } from 'react-elements/Header';

type Props = {
  returnUrl: ?string
};

class CollectConsents extends Component<Props> {
  render() {
    return (
      <div>
        <Header title="Thank you for creating a Guardian account" />
        {this.props.returnUrl && (
          <Button href={this.props.returnUrl}>Continue</Button>
        )}
      </div>
    );
  }
}

export { CollectConsents };
