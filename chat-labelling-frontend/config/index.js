'use strict'
// Template version: 1.3.1
// see http://vuejs-templates.github.io/webpack for documentation.

const path = require('path')
const cookieMap = {};
const port=8081
const host='0.0.0.0'
module.exports = {
  dev: {
    // Paths
    assetsSubDirectory: 'static',
    assetsPublicPath: '/',
    proxyTable: {
      '/checkLogin': {
        target: 'http://localhost:9090',
        secure: false,
        changeOrigin:true,
        onProxyRes: function(proxyRes, req, res) {
          var cookies = proxyRes.headers['set-cookie'];
          //修改cookie Path
          if (cookies) {
            var newCookie = cookies.map(function(cookie) {
              return cookie.replace('Path=/; HttpOnly', 'Path=/');
            });
            //修改cookie path
            delete proxyRes.headers['set-cookie'];
            proxyRes.headers['set-cookie'] = newCookie;
            cookieMap[req.get('User-Agent')] = proxyRes.headers['set-cookie'].join(';');
          }
        },
      },
      '/api': {
        target: 'http://localhost:9090',
        secure: false,
        changeOrigin:true,
        onProxyReq(proxyReq, req, res) {
          let cookie = cookieMap[req.get('User-Agent')] || '';
          proxyReq.setHeader('Cookie', cookie);
        }
      },
      '/websocket':{
        target: 'ws://localhost:9090',
        ws:true
      },
      '/test/**':{
        target:'http://'+host+':'+port+'/test.html',
        pathRewrite: function(path, req) {
          return '';
        }
      },
      '/':{
        bypass: function (req, res, proxyOptions) {

          if(req.url === '/test.html'||req.url.startsWith('/test')){
            //这里配置不需要进行登录的页面
            return true;
          }
          if ((req.url !== '/login.html'||req.url !== '/checkLogin') && !cookieMap[req.get('User-Agent')]) {
            res.redirect('/login.html');
            return true;
          }
          if(req.url === '/index'){//这里有多少页面最好加多少重定向
            res.redirect('/index.html');
            return true
          }
          if(req.url === '/login'){//这里有多少页面最好加多少重定向
            res.redirect('/login.html');
            return true
          }
        }
      }
    },
    // Various Dev Server settings
    host: host, // can be overwritten by process.env.HOST
    port: port, // can be overwritten by process.env.PORT, if port is in use, a free one will be determined
    autoOpenBrowser: false,
    errorOverlay: true,
    notifyOnErrors: true,
    poll: false, // https://webpack.js.org/configuration/dev-server/#devserver-watchoptions-

    // Use Eslint Loader?
    // If true, your code will be linted during bundling and
    // linting errors and warnings will be shown in the console.
    useEslint: true,
    // If true, eslint errors and warnings will also be shown in the error overlay
    // in the browser.
    showEslintErrorsInOverlay: false,

    /**
     * Source Maps
     */

    // https://webpack.js.org/configuration/devtool/#development
    devtool: 'cheap-module-eval-source-map',

    // If you have problems debugging vue-files in devtools,
    // set this to false - it *may* help
    // https://vue-loader.vuejs.org/en/options.html#cachebusting
    cacheBusting: true,

    cssSourceMap: true
  },

  build: {
    // Template for index.html
    index: path.resolve(__dirname, '../dist/index.html'),

    // Paths
    assetsRoot: path.resolve(__dirname, '../dist'),
    assetsSubDirectory: 'static',
    assetsPublicPath: '/',

    /**
     * Source Maps
     */

    productionSourceMap: true,
    // https://webpack.js.org/configuration/devtool/#production
    devtool: '#source-map',

    // Gzip off by default as many popular static hosts such as
    // Surge or Netlify already gzip all static assets for you.
    // Before setting to `true`, make sure to:
    // npm install --save-dev compression-webpack-plugin
    productionGzip: false,
    productionGzipExtensions: ['js', 'css'],

    // Run the build command with an extra argument to
    // View the bundle analyzer report after build finishes:
    // `npm run build --report`
    // Set to `true` or `false` to always turn it on or off
    bundleAnalyzerReport: process.env.npm_config_report
  }
}
