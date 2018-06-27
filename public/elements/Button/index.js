import { h, Component } from 'preact';
import arrow from 'components/form/arrow-right.svg';

type Props = {
  href: ?string,
};

class Button extends Component<Props> {
  render() {
    return this.props.href?(
      <a href={this.props.href} className="form-button form-button--main">
        {this.props.children}
        <span dangerouslySetInnerHTML={{__html:arrow}} />
      </a>
    ):(
      <button className="form-button form-button--main">
        {this.props.children}
        <span dangerouslySetInnerHTML={{__html:arrow}} />
      </button>
    )
  }
}

export { Button };
