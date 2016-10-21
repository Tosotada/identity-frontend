# Contributing to identity-frontend

1. Branch off of master, name your branch related to the feature you're
   implementing, or prefix with `fix-` for bug fixes
2. Do your thing
3. Ensure tests pass locally with `sbt test`
4. Make sure your branch is up to date with our master
  - by merging or (preferably, if possible) rebasing onto master
  - this makes sure any conflicts are resolved prior to code review
5. Open a pull request
6. Code will be reviewed and require a :+1: from a team member before it
   will be merged
7. The merger is required to ensure the change is deployed to production.
   Each merge is automatically deployed to our CODE environment, where it
   should be sanity checked before PROD deploy

If you have any questions, come chat to us or send us an email.


## Coding conventions

- 2 space indent, trimmed spaces, lf, utf8, enforced with
  [editorconfig](http://editorconfig.org/), please install the plugin for your
  editor
- **Commits:** Please don't squash, history is good
- **Scala:** [Scala style guide](http://docs.scala-lang.org/style/)
- **Views:** [handlebars](http://jknack.github.io/handlebars.java/) used, view
  inputs defined in view model objects, only use built in helpers if you can

### Structure

```
identity-frontend
├── app              - Scala Play application
└── public           - Client-side assets
    └── components   - Self-contained, reusable components
```

The **`app`** directory contains the Scala Play Application which runs the web application.

The **`public`** directory contains assets for the Client-side interface and
html responses. This directory should only contain resources for the primary
entry points (such as `main.css` or `main.js`). All other supporting resources
are within the **`public/components`** directory.

The **`public/components`** directory contains components for all pages within
the application. A component is a self-contained, reusable set of relating logic.
This can be groups of interface elements, or self contained libraries. All supporting
resources for a component should be within the component directory, regardless
of filetype or technology. So its not uncommon for a component directory to contain
Javascript modules, Handlebars views, CSS, and image assets. Directory structure
should be flat to be browsable, and component names should be simple and logical.

As convention, partial templates and CSS stylesheets are prefixed with an underscore.


### Javascript guidelines
Javascript source should be written in ES6 in the [Idiomatic JS](https://github.com/rwaldron/idiomatic.js)
style. This is enforced using [eslint](http://eslint.org/) when running tests.

ES6 is transpiled with [Babel](https://babeljs.io/) as part of a
[Webpack](http://webpack.github.io/) build step. The webpack build config
is defined in [`webpack.config.js`](https://github.com/guardian/identity-frontend/blob/master/webpack.config.js).

The build is triggered as part of sbt's compile stage. This is configured using
an `sbt-web` task in [`frontend.sbt`](https://github.com/guardian/identity-frontend/blob/master/frontend.sbt)
which will run `npm run build` when a frontend asset has changed.

### CSS guidelines
CSS source should be written using [Idiomatic CSS](https://github.com/necolas/idiomatic-css) style.
This is enforced using [stylelint](http://stylelint.io/) when running tests.

CSS is processed using [PostCSS](https://github.com/postcss/postcss) configured
using plugins defined in [postcss.json](https://github.com/guardian/identity-frontend/blob/master/postcss.json).

CSS is structured using [BEM](https://css-tricks.com/bem-101/) (Block-Element-Modifier):

    .[block]
    .[block]__[element]
    .[block]--[modifier]
    .[block]__[element]--[modifier]

If possible, CSS should only be applied to classes only. Ideally, only a single
class should be applied to an element. So when applying modifiers use a single
class on the element:

```html
<div class="main-block--highlighted">...</div>
```

Then in the CSS definition, `@extend` from the core block or element that the
modifier is applied to:

```css
.main-block--highlighted {
  @extend .main-block;
}
```

All size units should be expressed in `rem` ("root em") units as much as
possible.CSS is written to override the default base-font size to a
representative `10px`, so `1rem = 10px`. This is to improve accessibility by
allowing pages to scale if the user's browser has a larger font size set.

Pixel units should only be used when a constant size is required for User
Experience purposes, such as `border: 1px` on buttons.

Pixel fallbacks for `rem` units are added with PostCSS automatically via the
[cssnext](http://cssnext.io/) plugin. Vendor prefixes for "Modern" CSS are
also automatically added via PostCSS and cssnext with autoprefixer.


### Multi-Variant Tests
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

#### Recording test results
Test results will be recorded on page view automatically in Ophan.
But to have test results recorded correctly by the data team, a test definition
must be created in the [guardian/frontend]() repo.

See [ab-testing.md](https://github.com/guardian/frontend/blob/master/docs/ab-testing.md)
for more info, and [#11372](https://github.com/guardian/frontend/pull/11372) as
an example.

All tests are prefixed automatically with `ab` when recorded, and tests defined
in this repo are automatically namespaced with `Identity`.

#### Manually testing variants
Append `?mvt_<testName>=<variantId>` to a route with a `MultiVariantTestAction`.

### Test guidelines

- Tests should complete in under five minutes.
- Prefer unit tests to integration/functional tests.
- Unstable tests should be removed.
