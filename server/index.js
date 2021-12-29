const app = require('express')();
const server = require('http').createServer(app);
const io = require('socket.io')(server);
const port = process.env.PORT || 8080;

server.listen(port, () => {
  console.log('Server listening at port %d', port);
});