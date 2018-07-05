module.exports = {
  extends: ['airbnb', 'prettier', 'plugin:react/recommended'],
  plugins: ['prettier','import','flow'],
  env: {
    browser: true,
    es6: true
  },
  parserOptions: {
    ecmaVersion: 9,
  },
  parser: 'babel-eslint',
  settings : {
    'import/resolver': {
      webpack: {
        config: 'webpack.config.js'
      }
    }
  },
  rules: {
    'import/no-extraneous-dependencies': 'off',
    'import/prefer-default-export': 'off',
    'prettier/prettier': 'error',
    'jsx-quotes': ['error', 'prefer-double'],
    'no-extend-native': 'error',
    'no-param-reassign': ['error', { props: false }],
    'func-style': ['error', 'expression', { allowArrowFunctions: true }],
    'prefer-destructuring': 'off',
    'react/jsx-filename-extension': 'off',
    'react/prefer-stateless-function': 'off',
    'no-console': ['error', { allow: ['error'] }]
  },
  // don't look for eslintrcs above here
  root: true,
};
