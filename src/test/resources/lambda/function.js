const request = require('request');

    module.exports = { main: function (event, context) {
        return new Promise((resolve, reject) => {
            console.log("Calling lambda function.");
            const url = `http://httpbin.org/uuid`;

            sendReq(url, resolve, reject)
        })
    } }

    function sendReq(url, resolve, reject) {
        request.get(url, { json: true }, (error, response, body) => {
            if(error){
                console.log("Error occurred: " + error);
                resolve(error);
            }
            console.log("Response acquired successfully! Uuid: " + response.body.uuid);
            resolve(response);
        })
    }