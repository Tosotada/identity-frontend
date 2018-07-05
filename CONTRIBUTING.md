
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


## Coding conventions

- 2 space indent, trimmed spaces, lf, utf8, enforced with
  [Prettier](https://prettier.io/), please install the plugin for your
  editor if you can
- **Commits:** Please don't squash, history is good
- **Scala:** [Scala style guide](http://docs.scala-lang.org/style/)
- **Views:** [handlebars](http://jknack.github.io/handlebars.java/) used, view
  inputs defined in view model objects, only use built in helpers if you can

## Architecture
`identity-frontend` is primarily a Scala app, however some functionality is offered using javascript. Our line in the sand here is that **signing in or up must not require javascript**. This means you must make a judgement cal on how to build up new features, either as server-side code (Scala+HBS optionally hydrated with JS) or as client-side code (React)


### Javascript hydration
All javascript must attach itself to a existing HTML element with given classnames. Components are loaded in [js/components.js](https://github.com/guardian/identity-frontend/blob/master/public/js/components.js) and must export the selector they want to attach themselves to, and a `init` function that will receive the components. This abstracts away manually handling binding javascript and html together.

When putting new html in the page it is your responsibility to scan it for components, you can do this by importing `loadComponents` and passing it the new HTML. [Here's an example](https://github.com/guardian/identity-frontend/blob/5632078ea8bfe55d5fd7bf1acd340ada9c08ecdd/public/components/ajax-step-flow/ajax-step-flow.js#L188).

### Using React 
We use react in a similar fashion, attaching it to an existing HTML element. To simplify handling passing initial state and fallback rendering we have what we call "React Islands" (called islands because they are isolated divs).

You can define an island from an HBS template [like this](https://github.com/guardian/identity-frontend/blob/561eeda377ff3853057dbb903b91099a8bfb8b7a/public/collect-consents.hbs), islands in handlebars contain two things:
 
 - A fallback element that will render if javascript is disabled or slow and can contain a button to continue to a given URL if this is an optional part of a flow
 - A JSON bootstrap that contains the default props for the main react element to be rendered. 
 
Islands are hydrated like any other javascript element, however there's a small helper tool at `js/hydrate-react-island` that will automatically replace the island with the element and put in the props for you, []this is how that looks like](https://github.com/guardian/identity-frontend/blob/561eeda377ff3853057dbb903b91099a8bfb8b7a/public/components/react-island/react-island--collect-consents.js).  

Due to the overhead of React at the moment we are using async loading for islands, this keeps the main js bundle to an extremely small size. However, since the only entry point for react code is in the island components you don't have to bother yourself with async loading inside the react elements themselves, only at the component level.


#### CSS Modules
Whenever possible, React elements should import CSS as CSS Modules [like this](https://github.com/guardian/identity-frontend/blob/a53a696f3d352dd336539928c955376fb1106d1b/public/react-elements/Button.js) instead of using global class names. CSS used only by react elements should live alongside them instead of in `/components`


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
html responses. This directory should only contain resources for the primary
entry points (such as `main.css` or `main.js`). All other supporting resources
are within the **`public/components`** directory.

The **`public/components`** directory contains components for all pages within
the application. A component is a self-contained, reusable set of relating logic.
This can be groups of interface elements, or self contained libraries. 

The **`public/react-elements`** directory contains React elements. 

### Javascript guidelines
We use [Flow](https://flow.org/en/) to type check javascript. This happens at pre-push time but you can also manually test your types by running `npm run flow`. 

ES6 is transpiled with [Babel](https://babeljs.io/) as part of a
[Webpack](http://webpack.github.io/) build step. The webpack build config
is defined in [`webpack.config.js`](https://github.com/guardian/identity-frontend/blob/master/webpack.config.js).

The build is triggered as part of the npm scripts. This is configured using
the `watch` script in [`package.json`](https://github.com/guardian/identity-frontend/blob/master/package.json).

### CSS guidelines
CSS source should be written using [Medium's](https://medium.com/@fat/mediums-css-is-actually-pretty-fucking-good-b8e2a6c78b06) CSS style guide.

CSS is processed using [PostCSS](https://github.com/postcss/postcss) configured using plugins defined in [postcss.config.js](https://github.com/guardian/identity-frontend/blob/master/postcss.config.js).

CSS is structured using [BEM](https://css-tricks.com/bem-101/) (Block-Element-Modifier):

    .[block]
    .[block]__[element]
    .[block]--[modifier]
    .[block]__[element]--[modifier]

Try to keep CSS scoped to an **element level** and to keep elements as reusable as possible. In practical terms this mostly means setting the placement (margin, position) from a container.

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

Whenever possible try to stick with standard css syntax such as using `var(--color-main)` instead of `$color-main`. At the moment a couple of redundant postcss plugins exist within the projects but the aim is to eventually remove them and write standard CSS.

All size units should be expressed in `rem` ("root em") units as much as
possible.CSS is written to override the default base-font size to a
representative `10px`, so `1rem = 10px`. This is to improve accessibility by
allowing pages to scale if the user's browser has a larger font size set.

Pixel units should only be used when a constant size is required for User
Experience purposes, such as `border: 1px` on buttons.

Pixel fallbacks for `rem` units are added with PostCSS automatically via the
[cssnext](http://cssnext.io/) plugin. Vendor prefixes for "Modern" CSS are
also automatically added via PostCSS and `cssnext` with `autoprefixer`.


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
