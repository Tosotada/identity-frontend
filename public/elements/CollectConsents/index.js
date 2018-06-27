// @flow

import { h, Component } from 'preact';
import { Button } from 'elements/Button';
import { Header } from 'elements/Header';

class CollectConsents extends Component {
  render() {
    return (
      <div>
        <Header title="Thank you for creating a Guardian account" />
        <Button href="/test">Continue</Button>
        <pre>{JSON.stringify(this.props)}</pre>
      </div>
    );
  }
}

export { CollectConsents };
