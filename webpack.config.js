/*eslint-env node*/

var webpack = require( 'webpack' );
var path = require( 'path' );

module.exports = {
  entry: {
    main: './public/main'
  },
  output: {
    path: path.resolve( __dirname, 'target/web/build-npm/' ),
    publicPath: '/static/',
    filename: '[name].bundle.js'
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: [{
          loader: 'babel-loader'
        }]
      },
      {
        test: /\.css$/,
        exclude: /node_modules/,
        use: [{
          loader: 'raw-loader'
        }]
      },
      {
        test: /\.svg/,
        exclude: /node_modules/,
        use: [{
          loader: 'svg-inline-loader'
        }]
      }
    ]
  },
  plugins: [
    new webpack.optimize.MinChunkSizePlugin({
      minChunkSize: 99999,
    })
  ],
  resolve: {
    modules: [
      path.resolve(__dirname, 'public'),
      'node_modules'
    ],
    alias: {
      'intl-tel': 'intl-tel-input/build/js/intlTelInput',
      'intl-tel-utils': 'intl-tel-input/build/js/utils.js'
    }
  }
};
