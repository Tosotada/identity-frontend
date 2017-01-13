# identity-frontend

[![Circle CI](https://circleci.com/gh/guardian/identity-frontend/tree/master.svg?style=shield)](https://circleci.com/gh/guardian/identity-frontend/tree/master)

Web frontend for Sign in and Registration at [theguardian.com](http://theguardian.com).


# Application configuration

Configuration files:
- Environment-specific configuration (`conf/<ENV>.conf`)
- Application configuration (`conf/application.conf`)
- System file with additional properties (`/etc/gu/identity-frontend.conf` or `~/.gu/identity-frontend.conf`)

# Local development

## Nginx setup

1. Clone [identity-platform](https://github.com/guardian/identity-platform) project
1. Make sure you are in the base `identity-platform` directory
1. Ensure you have the correct [identity-platform hosts](https://github.com/guardian/identity-platform/blob/master/nginx/hosts) included in the `/etc/hosts` file on your machine
1. Run `sudo nginix/setup.sh identity`

## Configuration

Install the local configuration file from s3:

```
mkdir -p /etc/gu
aws s3 cp --profile identity s3://gu-identity-frontend-private/DEV/identity-frontend.conf /etc/gu
```

**Note**: If you do not have Janus access to Identity, we can grant your team specific access, which means you would substitute `--profile identity` with e.g. `--profile membership`. Contact the Identity team if you require access to these files.

You should now be able to start the application (`sbt run`), go to [https://profile-origin.thegulocal.com/management/healthcheck](https://profile-origin.thegulocal.com/management/healthcheck) and see a green padlock for your local SSL certificate as well as a 200 response.

## Running the application

Requires:

 - [JDK 8](http://openjdk.java.net)
 - [sbt](http://www.scala-sbt.org)
 - [Node.js 4.x](https://nodejs.org)

To run the application in development mode use:

    ./start-frontend.sh

This command will automatically pull down all dependencies for the Scala app,
and client-side dependencies with Node.js. Sources will automatically be watched,
so making changes locally will result in compile being triggered automatically.

Client side sources will automatically be compiled using the `npm run build` command.

## Development and Contributing
See [CONTRIBUTING.MD](https://github.com/guardian/identity-frontend/blob/master/CONTRIBUTING.md).

## Testing

To run unit tests:

    sbt test

To run functional selenium tests in a browser:

    sbt "project functional-tests" test

Credentials for social functional tests are in private `DEV/identity-frontend.conf`.
