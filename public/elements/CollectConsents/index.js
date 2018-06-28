// @flow

import { h, Component } from 'preact';
import { Button } from 'elements/Button';
import { Header } from 'elements/Header';

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
