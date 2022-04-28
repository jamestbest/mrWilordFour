const app = require('express')();
const server = require('http').createServer(app);
const io = require('socket.io')(server, {
  maxHttpBufferSize: 1e6,
  cors: true,
  allowEIO3: true,
  pingTimeout: 200000
});
const port = process.env.PORT || 8080;

players = [];

server.listen(8080 ,() => {
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

  console.log(players.length);

  socket.emit("socketID", {id: socket.id})

  if (players.length > 0){
    id = players[0].id;
    socket.to(id).emit("newPlayer")
  }

  socket.on("getUpdatedEntities", function(data){
    socket.broadcast.emit("getUpdatedEntities", data)
    // console.log("getUpdated Entities")
    // console.log(data)
  });

  socket.on("checkMovementSync", function(data){
    socket.broadcast.emit("checkMovementSync", data)
    console.log("checkMovementSync")
    console.log(data)
  });

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

  socket.on("joinRoom", function(data){
    socket.join(data)
    console.log(io.sockets.adapter.rooms)
    console.log(io.sockets.adapter.rooms.get(data));
    console.log(data)
  })

  socket.on("changeTileType", function(data){
    socket.broadcast.emit("changeTileType", data)
  })

  socket.on("changeThingType", function(x, y, type, height,emitsLight){
    socket.broadcast.emit("changeThingType", x, y, type, height, emitsLight)
  })

  socket.on("endGame", function(data){
    socket.broadcast.emit("endGame", data)
  });

  socket.on("updateGameSpeed", function(speed){
    socket.broadcast.emit("updateGameSpeed", speed)
  });

  socket.on("entityAttacking", function(attackerId, defenderId){
    socket.broadcast.emit("entityAttacking", attackerId, defenderId)
    console.log("entityAttacking")
  });

  socket.on("taskReservation", function(x, y, type){ //not used yet, also need one for when task is unreserved
    socket.broadcast.emit("taskReservation", x, y, type)
    console.log("taskReservation")
  });

  socket.on("completeTask", function(data){
    socket.broadcast.emit("completeTask", data)
    console.log("completeTask")
  });

  socket.on("updateTaskPercentage", function(data){
    socket.broadcast.emit("updateTaskPercentage", data)
    console.log("updateTaskPercentage" + data)
  });

  socket.on("setTasksFromSelection", function(taskType, taskSubType, minXCoord, minYCoord, maxXCoord, maxYCoord){
    socket.broadcast.emit("setTasksFromSelection", taskType, taskSubType, minXCoord, minYCoord, maxXCoord, maxYCoord)
    console.log("setTasksFromSelection" + taskType + " " + taskSubType + " " + minXCoord + " " + minYCoord + " " + maxXCoord + " " + maxYCoord)
  });

  socket.on("addTask", function(type, subtype, x, y){
    socket.broadcast.emit("addTask", type, subtype, x, y)
    console.log("addTask" + type + " " + subtype + " " + x + " " + y)
  });

  socket.on("addFire", function (x, y, name) {
    socket.broadcast.emit("addFire", x, y, name)
    console.log("addFire" + x + " " + y + " " + name)
  });

  socket.on("removeFire", function(x, y){
    socket.broadcast.emit("removeFire", x, y)
    console.log("removeFire" + x + " " + y)
  });

  socket.on("updatePriority", function(colonistID, priorityName, priorityValue){
    socket.broadcast.emit("updatePriority", colonistID, priorityName, priorityValue)
    console.log("updatePriority" + colonistID + " " + priorityName + " " + priorityValue)
  });

  socket.on("cancelTasksFromSelection", function(minXCoord, minYCoord, maxXCoord, maxYCoord){
    socket.broadcast.emit("cancelTasksFromSelection", minXCoord, minYCoord, maxXCoord, maxYCoord)
    console.log("cancelTasksFromSelection" + minXCoord + " " + minYCoord + " " + maxXCoord + " " + maxYCoord)
  });

  socket.on("colonistTask", function(data){
    socket.broadcast.emit("colonistTask", data)
    console.log("colonistTask")
  });

  socket.on("addFloorDrop", function(x, y, type, amount){
    socket.broadcast.emit("addFloorDrop", x, y, type, amount)
    console.log("addFloorDrop")
  });

  socket.on("removeFloorDrop", function(x, y, type){
    socket.broadcast.emit("removeFloorDrop", x, y, type)
    console.log("removeFloorDrop")
  });

  socket.on("updateFloorDrop", function(x, y, type, amount){
    socket.broadcast.emit("updateFloorDrop", x, y, type, amount)
    console.log("updateFloorDrop")
  });

  socket.on("addZone", function(x, y, x2, y2){
    socket.broadcast.emit("addZone", x, y, x2, y2)
    console.log("addZone")
  });

  socket.on("removeZone", function(x, y, x2, y2){
    socket.broadcast.emit("removeZone", x, y, x2, y2)
    console.log("removeZone")
  });

  socket.on("syncTime", function(data){
    socket.broadcast.emit("syncTime", data)
    console.log("syncTime")
  });

  socket.on("addDropToZone", function(x, y, type, amount){
    socket.broadcast.emit("addDropToZone", x, y, type, amount)
    console.log("addDropToZone")
  });

  socket.on("spawnMobs", function(data){
    socket.broadcast.emit("spawnMobs", data)
    console.log("spawnMobs" + data)
  });

  socket.on("destroyMobs", function(data){
    socket.broadcast.emit("destroyMobs", data)
    console.log("destroyMobs" + data)
  });

  socket.on("playSound", function(soundName, x, y){
    socket.broadcast.emit("playSound", soundName, x, y)
    console.log("playSound" + soundName, x, y)
  });

  socket.on("stopSound", function(soundName, x, y){
    socket.broadcast.emit("stopSound", soundName, x, y)
    console.log("stopSound" + soundName, x, y)
  });

  socket.on("addNoti", function(data){
    socket.broadcast.emit("addNoti", data)
    console.log("addNoti" + data)
  });

  socket.on("removeNoti", function(data){
    socket.broadcast.emit("removeNoti", data)
    console.log("removeNoti" + data)
  });

  socket.on("spawnBarbarians", function(data){
    socket.broadcast.emit("spawnBarbarians", data)
    console.log("spawnBarbarians" + data)
  });

  socket.on("updateHealth", function(enitityID, health){
    socket.broadcast.emit("updateHealth", enitityID, health)
    console.log("updateHealth" + enitityID + " " + health)
  });
});