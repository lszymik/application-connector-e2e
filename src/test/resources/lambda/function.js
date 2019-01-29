const request = require('request');

    module.exports = { main: function (event, context) {
        return new Promise((resolve, reject) => {
            console.log("Calling lambda function.");
            const url = `APP_URL`;

            sendReq(url, resolve, reject)
        })
    } }

    function sendReq(url, resolve, reject) {
        request.post(url, { json: true }, (error, response, body) => {
            if(error){
                console.log("Error occurred: " + error);
                resolve(error);
            }
            console.log("Counter increased. Response acquired successfully!");
            resolve(response);
        })
    }