/*eslint-env node*/
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const webpack = require('webpack');
const path = require('path');
const { StatsWriterPlugin } = require("webpack-stats-plugin")

module.exports = (env, argv) => {

  const getCssLoaderConfig = modules => [
    argv.mode === 'development' ? 'style-loader' : MiniCssExtractPlugin.loader,
    {
      loader: 'css-loader',
      options: {
        modules,
        import: false,
        importLoaders: 1
      }
    },
    'postcss-loader'
  ];

  return {
    entry: {
      main: './public/main'
    },
    output: {
      path: path.resolve(__dirname, 'target/web/build-npm/'),
      publicPath: '/static/',
      filename: '[name].bundle.js'
    },
    module: {
      rules: [
        {
          test: /\.js$/,
          exclude: /node_modules/,
          use: [
            {
              loader: 'babel-loader'
            }
          ]
        },
        {
          test: /\.css$/,
          exclude: [/node_modules/, /main\.css$/],
          use: getCssLoaderConfig(true)
        },
        {
          test: /main\.css$/,
          exclude: /node_modules/,
          use: getCssLoaderConfig(false)
        },
        {
          test: /\.svg/,
          exclude: /node_modules/,
          use: [
            {
              loader: 'svg-inline-loader'
            }
          ]
        }
      ]
    },
    plugins: [
      new webpack.optimize.MinChunkSizePlugin({
        minChunkSize: 99999
      }),
      new MiniCssExtractPlugin({
        filename: 'bundle.css',
        chunkFilename: '[id].bundle.css'
      }),
      new webpack.DefinePlugin({
        'process.env.NODE_ENV': argv.mode
      }),
      new StatsWriterPlugin({
        filename: "stats.json",
        fields: null
      })
    ],
    optimization: {
      minimizer: [
        new OptimizeCSSAssetsPlugin({})
      ]
    },
    resolve: {
      modules: [path.resolve(__dirname, 'public'), 'node_modules'],
      alias: {
        'intl-tel': 'intl-tel-input/build/js/intlTelInput',
        'intl-tel-utils': 'intl-tel-input/build/js/utils.js'
      }
    }
  };
};
