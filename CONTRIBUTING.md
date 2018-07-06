
```
                 ┏━━━━━━━━━┅┅~~ ~
                 ┃ Thanks for contributing!
                 ┗━━━━━━━━━┅┅~~ ~
     /\**/\       │
    ( o_o  )_     │
     (u--u   \_)  │
      (||___   )==\
    ,dP"/b/=( /P"/b\
    |8 || 8\=== || 8
    `b,  ,P  `b,  ,P
      """`     """`
```

# Contributing to identity-frontend

1. Branch off of master, name your branch related to the feature you're
   implementing, prefix with your initials/name (`bikercat-feature-name`,`bc-feature-name`)
2. Do your thing
3. Ensure tests pass locally with `sbt test`
4. Make sure your branch is up to date with master
    - by merging or (preferably, if possible) rebasing onto master
    - this makes sure any conflicts are resolved prior to code review
5. Open a pull request
6. Code will be reviewed and require a :+1: from a team member before it
   will be merged
7. The merger is required to ensure the change is deployed to production.

If you have any questions, come chat to us (`Digital/Identity` in hangouts) or send us [an email](identitydev@theguardian.com).

## Architecture overview
`identity-frontend` is primarily a Scala app. However, some functionality is offered using Javascript. Our line in the sand here is that **signing in or up must not require javascript**. This means you must make a judgement call on how to build up new features, either as server-side code (Scala+HBS optionally hydrated with JS) or as client-side code (React)


### Javascript
Since most javascript operations involve hydrating an existing element, and directly querying the DOM is error-prone, there's a small abstraction layer that handles hydrating elements and attaching your Javascript code to all matching elements on the page.

All javascript must attach itself to a existing HTML element with a given CSS classname. Components are manually loaded from a list in [js/components.js](https://github.com/guardian/identity-frontend/blob/master/public/js/components.js), when you write a component you must export the following, then add it to the component list.
 
 - An `init` function that will receive the html element.
 - A CSS `selector` your components wants to attach itself to
 - There's also `initOnce` which is optional and runs only once in the app lifecycle regardless of how many elements your component hits.

```js
//hello-world.js
export const init = ($target) => $target.innerText = 'hello world'; 
export const initOnce = () => alert('this happens once, ever');
export const selector = '.hello-world';

//hello-world.html
<div class="hello-world"></div>
```

When putting new HTML in the page it is your responsibility to scan it for components, you can do this by importing `loadComponents` and passing it the new HTML:

```js
import { loadComponents } from 'js/load-components';

export const init = ($target) => {
  fetchHtml('./my-awesome-page.html').then( html => {
    $target.innerHTML = html;
    loadComponents($target);
  })
} 
```

It's up to you to decide how much Javascript to use on a rendered component. If you want to use progressive enhancement on the existing content you will want traditional javascript. However, for more complex operations you should consider using React as follows:

### Using React 
We use React inside this component system, attaching it to an existing HTML element. To simplify handling passing initial state and fallback rendering we have what we call "React Islands" (called islands because they are isolated divs).

React elements themselves go inside `/react-elements` as they are architecturally different from traditional components and mixing them could lead to confusion. This also helps prevent potential 'mixing and matching' of react elements with traditional components as much as possible.

You can define an island from an HBS template:
```html
<div class="react-island react-island--name-to-look-for-in-js">
  {{> components/react-island/_react-island__fallback text=reactIslandFallbackText returnUrl=returnUrl }}
  {{> components/react-island/_react-island__bootstrap json=bootstrap }}
</div>
```

Islands in handlebars contain two things:
 
 - A fallback element that will render if javascript is disabled or slow and can contain a button to continue to a given URL if this is an optional part of a flow
 - A JSON bootstrap that contains the default props for the main react element to be rendered. 
 
Islands are hydrated like any other javascript element. However, there's a small helper tool at `js/hydrate-react-island` that will automatically replace the island with the element and put in the props for you, this is how that looks like:

```js
export const init = ($component): void => {
  Promise.all([
    import('js/hydrate-react-island'),
    import('react-elements/CollectConsents'),
  ]).then(([{ hydrate }, { CollectConsents }]) => {
    hydrate($component, CollectConsents);
  });
};
```

Due to the overhead of React at the moment we are using async loading for islands, this keeps the main js bundle to an extremely small size. However, since the only entry point for react code is in the island components you don't have to bother yourself with async loading inside the react elements themselves, only at the component level.

### CSS 
All global critical CSS is loaded from [`js/load-global-css.js`](https://github.com/guardian/identity-frontend/blob/lg-islands-stage-2/public/js/load-global-css.js). All standard imports there will go in the main CSS bundle while the async imports will be chunked using webpack rules.

We use [CSS Modules](https://github.com/css-modules/css-modules) for all CSS files. There is, however, a webpack rule to treat filenames `(file).global.css` as fully global CSS for Scala+HBS components. 

#### When using CSS in Javascript
Javascript elements (react or traditional) can import their own CSS as a normal import. You will get an object with all CSS module names as the children. Locally scoped CSS Modules (vs global ones) are preferable for all use cases where it's not needed to use the CSS on server-side rendered templates.

```js
import css from 'Card.css';

export const render = ({title}) => `
  <div class="${css.card}">
    <header class="${css.title}">${title}</div>
  </div>
`
```

Components that replicate existing static elements should use the existing global css class names. CSS used only by React elements should live alongside them in `/react-elements` instead of in `/components`
```js
export const render = ({name}) => `
  <button class="form-button">
    ${name}
  </button>
`
```


#### When using global CSS
Global CSS uses [BEM](https://css-tricks.com/bem-101/) (Block-Element-Modifier):

    .[block]
    .[block]__[element]
    .[block]--[modifier]
    .[block]__[element]--[modifier]

Try to keep CSS scoped to an **element level** and to keep elements as reusable as possible. In practical terms, this mostly means setting the placement (margin, position) from the container instead of from the element itself.

```css
/* no 😿 */
.ui-button {
  display: block;
  background: var(--color-button);
  position: absolute;
  bottom: 0;
}

/* yes 😻 */
.ui-button {
  display: block;
  background: var(--color-button);
}
.ui-dialog .ui-button {
  position: absolute;
  bottom: 0;
}
```


## Structure
```
identity-frontend
├── app                 - Scala Play application
└── public              - Client-side assets
    └── css             - Global CSS helpers
    └── js              - Global JS helpers
    └── components      - HBS Components
    └── react-elements  - React elements
```

The **`app`** directory contains the Scala Play Application which runs the web application.

The **`public`** directory contains assets for the Client-side interface and
HTML responses. This directory should only contain resources for the primary
entry points (such as `main.css` or `main.js`). All other supporting resources
are within the **`public/components`** directory.

The **`public/components`** directory contains components for all pages within
the application. A component is a self-contained, reusable set of relating logic.
This can be groups of interface elements, or self-contained libraries. 

The **`public/react-elements`** directory contains React elements. 

### Javascript processing
We use [Flow](https://flow.org/en/) to type check javascript. This happens at pre-push time but you can also manually test your types by running `npm run flow`. 

ES6 is transpiled with [Babel](https://babeljs.io/) as part of a
[Webpack](http://webpack.github.io/) build step. The webpack build config
is defined in [`webpack.config.js`](https://github.com/guardian/identity-frontend/blob/master/webpack.config.js).

The build is triggered as part of the NPM scripts. This is configured using
the `watch` script in [`package.json`](https://github.com/guardian/identity-frontend/blob/master/package.json).

### CSS Processing
CSS is processed using Webpack and [PostCSS](https://github.com/postcss/postcss) configured using plugins defined in [postcss.config.js](https://github.com/guardian/identity-frontend/blob/master/postcss.config.js) and loaders defined in [`webpack.config.js`](https://github.com/guardian/identity-frontend/blob/master/webpack.config.js).

As much as possible we stick to the CSS spec by using `postcss-preset-env`. This means native variables and calc for example. A huge gotcha you will fall into is nesting requires to have `&` to work in all rules. There's also mixins (but try not to use function mixins) and asset bundling.

## Coding conventions

- 2 space indent, trimmed spaces, lf, utf8, enforced with
  [Prettier](https://prettier.io/), please install the plugin for your
  editor if you can
- **Commits:** Please don't squash, history is good
- **Scala:** [Scala style guide](http://docs.scala-lang.org/style/)
- **Views:** [handlebars](http://jknack.github.io/handlebars.java/) used, view inputs defined in view model objects, only use built-in helpers if you can. Pass variables explicitly through imports rather than inheriting them. 

## Multi-Variant Tests
All Multi-Variant tests are defined server-side in [MultiVariantTests.scala](https://github.com/guardian/identity-frontend/blob/master/app/com/gu/identity/frontend/configuration/MultiVariantTests.scala).

For example:
```scala
case object MyABTest extends MultiVariantTest {
  val name = "MyAB"
  val audience = 0.2
  val audienceOffset = 0.6
  val isServerSide = true
  val variants = Seq(MyABTestVariantA, MyABTestVariantB)
}

case object MyABTestVariantA extends MultiVariantTestVariant { val id = "A" }
case object MyABTestVariantB extends MultiVariantTestVariant { val id = "B" }

object MultiVariantTests {
  def all: Set[MultiVariantTest] = Set(MyABTest)
}
```
Which creates a test with two variants against 20% of the audience, using
the segment of users with ids from 60% to 80% of the population.

When using a server-side only test, the `MultiVariantTestAction` action
composition should be used to access which tests are active for the
user for a particular route.

```scala
def myAction() = MultiVariantTestAction { request =>

  val tests: Map[MultiVariantTest, MultiVariantTestVariant] = request.activeTests

  // do things with the active tests
}
```
`MultiVariantTestAction` will force the response to be non-cacheable.

Each `MultiVariantTestAction` must also work without any active tests.

**Client-side** only tests are Javascript only, and should be cacheable. To
access test results for client-side tests, use:

```js
import { getClientSideActiveTestResults } from 'components/analytics/mvt';

const results = getClientSideActiveTestResults();
```

### Recording test results
Test results will be recorded on page view automatically in Ophan.
But to have test results recorded correctly by the data team, a test definition
must be created in the [guardian/frontend]() repo.

See [ab-testing.md](https://github.com/guardian/frontend/blob/master/docs/ab-testing.md)
for more info, and [#11372](https://github.com/guardian/frontend/pull/11372) as
an example.

All tests are prefixed automatically with `ab` when recorded, and tests defined
in this repo are automatically namespaced with `Identity`.

### Manually testing variants
Append `?mvt_<testName>=<variantId>` to a route with a `MultiVariantTestAction`.

### Test guidelines

- Tests should complete in under five minutes.
- Prefer unit tests to integration/functional tests.
- Unstable tests should be removed.
