# identity-frontend

Frontend for Identity account sign in and registration at [theguardian.com](http://theguardian.com).

## Configuration files

- Environment-specific configuration (`conf/<ENV>.conf`)
- Application configuration (`conf/application.conf`)
- Private configuration (`/etc/gu/identity-frontend.conf`)

## Local development

### Nginx setup

Clone [identity-platform](https://github.com/guardian/identity-platform) and follow its [nginx README](https://github.com/guardian/identity-platform/blob/master/nginx/README.md)

### Configuration

Download DEV private configuration from s3:

```bash
aws s3 cp --profile identity s3://identity-private-config/DEV/identity-frontend/identity-frontend.conf /etc/gu
```

**Note**: If you do not have Janus access to Identity, we can grant your team specific access, which means you would substitute `--profile identity` with e.g. `--profile membership`. Contact the Identity team if you require access to these files.


### Running

Requires:

 - [JDK 8](http://openjdk.java.net)
 - [sbt](http://www.scala-sbt.org)
 - [Node.js](https://nodejs.org) - version is specified by [.nvmrc](.nvmrc), run [`$ nvm use`](https://github.com/creationix/nvm#nvmrc) to use it
 
If this is the first time using the application you will need to pull down the client side dependencies using

    npm install

After that, to run the application in development mode and watch for changes you can use

    npm run watch

This command will automatically pull down all dependencies for the Scala app, and run the client side asset packing and sbt in parallel in the background.
    
Test by hitting [https://profile.thegulocal.com/management/healthcheck](https://profile.thegulocal.com/management/healthcheck). 

## Contributing

See [CONTRIBUTING.MD](https://github.com/guardian/identity-frontend/blob/master/CONTRIBUTING.md) for info on how the architecture is laid out.

## Running against DEV Identity API

By default identity-frontend targets CODE Identity API. To target DEV Identity API update `/etc/gu/identity-frontend.conf` to include:

```hocon
identity.api.host=idapi.thegulocal.com
identity.api.key=frontend-dev-client-token
```

## Running Tests

* Unit tests: `sbt test`
* Functional tests: `sbt "project functional-tests" test`
    
