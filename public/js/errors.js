class BackendError extends Error {
  errors: string[];
  constructor(errors) {
    super('ERR_BACKEND_ERROR');
    // $FlowFixMe
    this.constructor = BackendError;
    // $FlowFixMe
    this.__proto__ = BackendError.prototype; // eslint-disable-line no-proto
    this.errors = errors;
  }
}

class ContextError extends Error {
  context: {};
}

class MalformedResponseError extends ContextError {
  constructor(request) {
    super('ERR_MALFORMED_RESPONSE');
    // $FlowFixMe
    this.constructor = MalformedResponseError;
    // $FlowFixMe
    this.__proto__ = MalformedResponseError.prototype; // eslint-disable-line no-proto
    this.context = {
      request
    };
  }
}

class MalformedHtmlError extends ContextError {
  constructor(html) {
    super('ERR_MALFORMED_HTML');
    // $FlowFixMe
    this.constructor = MalformedHtmlError;
    // $FlowFixMe
    this.__proto__ = MalformedHtmlError.prototype; // eslint-disable-line no-proto
    this.context = {
      html
    };
  }
}

export {
  MalformedResponseError,
  MalformedHtmlError,
  BackendError,
  ContextError
};
