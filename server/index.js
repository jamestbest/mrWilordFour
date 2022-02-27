const app = require('express')();
const server = require('http').createServer(app);
const io = require('socket.io')(server, {
  maxHttpBufferSize: 1e11,
  cors: true,
  allowEIO3: true,
  pingTimeout: 200000
});
const port = process.env.PORT || 8080;

players = [];

server.listen(port, () => {
  console.log('Server listening at port %d', port);
});

io.on("connect_error", (err) => {
  console.log(`connect_error due to ${err.message}`);
});

//cd to server
//set DEBUG=*
//node index.js

io.on('connection', function(socket){
  console.log('a user connected');
  players.push(socket);

  socket.emit("socketID", {id: socket.id})
  socket.broadcast.emit("newPlayer")

  socket.on("updateColonists", function(data){
    players[0].emit("updateColonists", data)
    console.log("updateColonists")
  })

  socket.on("getUpdated Colonist", function(data){
    socket.broadcast.emit("getUpdated Colonist", data)
    console.log("getUpdated Colonist")
  })

  socket.on("connect_error", (err) => {
    console.log(`connect_error due to ${err.message}`);
  });

  socket.on('disconnect', function(){
    console.log('user disconnected ' + socket.id);
    players.splice(players.indexOf(socket), 1);
  });

  socket.on("test", (data) => {
    socket.broadcast.emit("testTwo", data)
  });

  socket.on("loadWorld", data =>{
    players[players.length - 1].emit("loadWorldClient", data)
  })

  socket.on("loadColonists", function(data){
    players[players.length - 1].emit("loadColonists", data)
  })

  socket.on("joinRoom", function(data){
    socket.join(data)
    console.log(io.sockets.adapter.rooms)
    console.log(io.sockets.adapter.rooms.get(data));
    console.log(data)
  })

  socket.on("changeTileType", function(data){
    socket.broadcast.emit("changeTileType", data)
  })
});