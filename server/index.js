const app = require('express')();
const server = require('http').createServer(app);
const io = require('socket.io')(server, {
  maxHttpBufferSize: 1e8
});
const port = process.env.PORT || 8080;

players = [];

server.listen(port, () => {
  console.log('Server listening at port %d', port);
});

io.on('connection', function(socket){
  console.log('a user connected');
  players.push(socket);

  socket.emit("socketID", {id: socket.id})
  socket.broadcast.emit("newPlayer")

  socket.on('disconnect', function(){
    console.log('user disconnected' + socket.id);
    players.splice(players.indexOf(socket), 1);
  });

  socket.on("loadWorld", function(data){
    players[players.length - 1].emit("loadWorld", data)
  })

  socket.on("joinRoom", function(data){
    socket.join(data)
    console.log(io.sockets.adapter.rooms.get(data));
    console.log(data)
  })
});