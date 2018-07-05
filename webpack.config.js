/*eslint-env node*/
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');
const webpack = require('webpack');
const path = require('path');
const { StatsWriterPlugin } = require('webpack-stats-plugin');

const getCssLoaderConfig = (modules, isDev) => [
  MiniCssExtractPlugin.loader,
  {
    loader: 'css-loader',
    options: {
      modules,
      sourceMap: isDev,
      importLoaders: 1
    }
  },
  {
    loader: 'postcss-loader',
    options: {
      sourceMap: isDev
    }
  }
];

module.exports = (env, argv) => {

  const isDev = argv && argv.mode === 'development';
  const globalCssModules = ['main.css',/\/components\/(.*)\.css$/];

  return {
    entry: {
      main: './public/main'
    },
    output: {
      path: path.resolve(__dirname, 'target/web/build-npm/'),
      publicPath: '/static/',
      filename: '[name].jaffa.js',
      chunkFilename: '[name].[chunkhash].jaffa.js'
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
          exclude: [/node_modules/, ...globalCssModules],
          use: getCssLoaderConfig(true, isDev)
        },
        {
          test: globalCssModules,
          exclude: /node_modules/,
          use: getCssLoaderConfig(false, isDev)
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
        filename: 'main.jaffa.css',
        chunkFilename: '[id].[chunkhash].jaffa.css'
      }),
      new StatsWriterPlugin({
        filename: 'stats.json',
        fields: null
      })
    ],
    optimization: {
      minimizer: [
        new OptimizeCSSAssetsPlugin({
          cssProcessorOptions: {
            discardUnused: {
              fontFace: false
            }
          }
        }),
        new UglifyJsPlugin({
          sourceMap: true /*sentry wants this*/
        })
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
