import { h, Component } from 'preact';

class Button extends Component {
  render() {
    return (
      <button className="form-button form-button--main">
        {children}
      </button>
    )
  }
}

export default CollectConsents;
