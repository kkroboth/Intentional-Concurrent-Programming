const path = require('path')
const webpack = require('webpack')

module.exports = {
    entry: {
        vendor: ['vue', 'vuetify', 'vuetify/dist/vuetify.min.css'],
        app: "./src/apps/main/index.js",
        login: "./src/apps/login/index.js"
    },
    output: {
        path: path.resolve(__dirname, '..', 'dist'),
        filename: '[name].bundle.js'
    },
    plugins: [
        new webpack.optimize.CommonsChunkPlugin({
            name: 'vendor',
            minChunks: Infinity
        })
    ],
    module: {
        rules: [
            {
                enforce: 'post',
                test: /\.vue$/,
                exclude: /(node_modules)/,
                loader: 'vue-loader'
            },
            {
                test: /\.js$/,
                exclude: /(node_modules|bower_components)/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ['@babel/preset-env'],
                        plugins: [require('@babel/plugin-proposal-object-rest-spread')]
                    }
                }
            },
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.(png|jp(e*)g|svg)$/,
                use: [{
                    loader: 'url-loader',
                    options: {
                        limit: 8000, // Convert images < 8kb to base64 strings
                        name: 'images/[hash]-[name].[ext]'
                    }
                }]
            }
        ]
    },
    watchOptions: {
        ignored: /node_modules/
    },

    devtool: 'source-map'
}