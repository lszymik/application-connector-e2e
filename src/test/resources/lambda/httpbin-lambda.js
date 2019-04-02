const request = require('request');

module.exports = { main: function (event, context) {

    return new Promise((resolve, reject) => {
        const url = `${process.env.GATEWAY_URL}/get`;
        const options = {
            url: url,
        };

        sendReq(url, resolve, reject)
    })
} }

function sendReq(url, resolve, reject) {
    request.get(url, { json: true }, (error, response, body) => {
        console.log(body)
        if(error){
            resolve(error)
        }
        resolve(body)
    })
}