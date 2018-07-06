const postcss = require('postcss');

module.exports = {
  "plugins": {
    "postcss-import":{
      path: `${__dirname}/public/`
    },
    "postcss-mixins": {
      mixins: {
        font: function (mixin, family, fontSet, filebase, weight = 400, style = 'normal') {
          const path = `v0/${fontSet}/${family}/${filebase}`;
          return {
            '@font-face': {
              'font-family': family,
              'src': `inline("${path}.woff") format("woff"), resolve("${path}.ttf") format("truetype")`,
              'font-weight': weight,
              'font-style': style,
              'font-stretch': 'normal'
            }
          }
        }
      }
    },
    "postcss-assets":{
      basePath: `${__dirname}/public/`,
      loadPaths: [`${__dirname}/public/components/**`],
      baseUrl: `/static/`,
    },
    "postcss-strip-units":{},
    "postcss-preset-env":{
      stage: 2,
      features: {
        'nesting-rules': true,
        'custom-properties': true,
        'custom-media-queries': true,
      }
    },
  }
}
