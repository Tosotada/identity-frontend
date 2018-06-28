// @flow

import { h, Component } from 'preact';
import css from 'components/layout/_layout.css';

type Props = {
  title: string
};

class Header extends Component<Props> {
  render() {
    return (
      <header className={css['layout-header']}>
        <h1 className={css['layout-header__title']}>{this.props.title}</h1>
      </header>
    );
  }
}

export { Header };
