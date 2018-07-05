module.exports = {
  "plugins": {
    "postcss-import":{
      path: `${__dirname}/public/`
    },
    "precss":{
      "import": {disable: true},
      "mixins": {},
      "media": {},
      "properties": {},
      "minmax": {disable: true},
      "color": {disable: true},
      "nesting": {},
      "nested": {},
      "selectors": {},
      "atroot": {disable: true},
      "lookup": {disable: true},
      "extend": {},
      "matches": {disable: true},
      "not": {disable: true}
    },
    "postcss-assets":{
      basePath: `${__dirname}/public/`,
      loadPaths: [`${__dirname}/public/components/**`],
      baseUrl: `/static/`,
    },
    "postcss-strip-units":{},
    "postcss-cssnext":{},
    "postcss-color-function":{},
  }
}
