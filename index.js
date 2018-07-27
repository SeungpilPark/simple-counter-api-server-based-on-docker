const express = require('express')
const app = express()
const { spawn } = require('child_process');

app.get('/', (request, response) => {

    var count = request.param('count');
    console.log(count);

    const command = spawn('docker-compose', ['scale', 'counter-service=' + count]);
    command.stdout.on('data', (data) => {
        console.log(`stdout: ${data}`);
    });

    command.stderr.on('data', (data) => {
        console.log(`stderr: ${data}`);
    });

    command.on('close', (code) => {
        response.json({
            result: `child process exited with code ${code} , requested server count ${count}`
        });
    });
});

app.listen(3000)