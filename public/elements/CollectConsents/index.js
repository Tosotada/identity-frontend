import { h, Component } from 'preact';

class CollectConsents extends Component {
  render() {
    return (
      <div>
        <marquee>Hello</marquee>
        <pre>
          {JSON.stringify(this.props)}
        </pre>
      </div>
    )
  }
}

export default CollectConsents;
