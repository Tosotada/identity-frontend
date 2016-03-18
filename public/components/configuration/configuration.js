/*global console*/

import { getElementById } from '../browser/browser';

class Configuration {
  constructor( omnitureAccount, sentryDsn, mvtTests, appVersion ) {
    this.omnitureAccount = omnitureAccount;
    this.sentryDsn = sentryDsn;
    this.mvtTests = mvtTests;
    this.appVersion = appVersion;
  }

  static fromObject( { omnitureAccount, sentryDsn, mvtTests, appVersion } ) {
    return new Configuration( omnitureAccount, sentryDsn, mvtTests, appVersion );
  }

  static fromDocument() {
    const configElem = getElementById( 'id_config' );
    if ( configElem !== undefined ) {
      const html = configElem.innerHTML();
      const parsed = parseJSON( html );

      return Configuration.fromObject( parsed );
    }
  }
}


class RuntimeParameters {
  constructor( activeTests ) {
    this.activeTests = activeTests;
  }

  static fromObject( { activeTests } ) {
    return new RuntimeParameters( activeTests );
  }

  static fromDocument() {
    const configElem = getElementById( 'id_runtime_params' );
    if ( configElem !== undefined ) {
      const html = configElem.innerHTML();
      const parsed = parseJSON( html );

      return RuntimeParameters.fromObject( parsed );
    }
  }
}


function parseJSON( input ) {
  try {
    return JSON.parse( input );

  } catch ( err ) {
    if ( console ) {
      console.warn( 'Could not parse Configuration JSON: ' + err );
    }

    return undefined;
  }
}


export const configuration = Configuration.fromDocument();
export const runtimeParameters = RuntimeParameters.fromDocument();
