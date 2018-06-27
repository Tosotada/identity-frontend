import { h, Component } from 'preact';

type Props = {
  title: string
};

class Header extends Component<Props> {
  render() {
    return (
      <header class="layout-header">
        <h1 class="layout-header__title">{this.props.title}</h1>
      </header>
    )
  }
}

export { Header };
