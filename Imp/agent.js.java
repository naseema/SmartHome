
statDev1 <- -1;
statDev2 <- -1;
statDev3 <- -1;
statDev4 <- -1;
statDev5 <- -1;
statDev6 <- -1;

statOnline <- 0;


function requestHandler(request, response) {
    // if (device.isconnected()){
    //     // if device is connected, set backgroundColor to green
    //     server.log("online");
    // } 
    // else {
    //     // if device is disconnected, set background color to red
    //     server.log("offline");
    // }





  try {
    // Check if your Imp connecting by sending him message...
    device.send("isOnline","");
    if (statOnline == 0) {
        server.log("!!!!!!!!!!!!!!!!!!!!device disconnected from agent");
        response.send(200, ":(");
        statDev1 <- -1;
        statDev2 <- -1;
        statDev3 <- -1;
        statDev4 <- -1;
        statDev5 <- -1;
        statDev6 <- -1;
        return;
    }
    statOnline = 0;

    
    if ("status" in request.query) {
        response.send(200, statDev1 + "," + statDev2 + ","+ statDev3 
        + ","+ statDev4 + ","+ statDev5 + ","+ statDev6);
        server.log("### Device status :" + statDev1 + "," + statDev2 
        + ","+ statDev3 + ","+ statDev4 + ","+ statDev5 + ","+ statDev6);
        // getting pin status from Imp
        device.send("status",""); 
        return;
    } else    
        // server.log(request.query.dev1);
    // check if the user sent dev1 as a query parameter
    if ("dev1" in request.query) {
      
      // if they did, and dev1=1.. set our variable to 1
      if (request.query.dev1 == "1" || request.query.dev1 == "0") {
        // convert the dev1 query parameter to an integer
        local dev1 = request.query.dev1.tointeger();
 
        // send "dev1" message to device, and send ledState as the data
        device.send("dev1", dev1); 
        device.send("status",""); 
      }
    } else if ("dev2" in request.query) {
      
      // if they did, and dev2=1.. set our variable to 1
      if (request.query.dev2 == "1" || request.query.dev2 == "0") {
        // convert the dev2 query parameter to an integer
        local dev2 = request.query.dev2.tointeger();
 
        // send "dev2" message to device, and send ledState as the data
        device.send("dev2", dev2); 
        device.send("status",""); 
      }
    
    } else if ("dev2" in request.query) {
      
      // if they did, and dev2=1.. set our variable to 1
      if (request.query.dev2 == "1" || request.query.dev2 == "0") {
        // convert the dev2 query parameter to an integer
        local dev2 = request.query.dev2.tointeger();
 
        // send "dev2" message to device, and send ledState as the data
        device.send("dev2", dev2); 
        device.send("status",""); 
      }
    
    } else if ("dev3" in request.query) {
      
      // if they did, and dev3=1.. set our variable to 1
      if (request.query.dev3 == "1" || request.query.dev3 == "0") {
        // convert the dev2 query parameter to an integer
        local dev3 = request.query.dev3.tointeger();
 
        // send "dev2" message to device, and send ledState as the data
        device.send("dev3", dev3); 
        device.send("status",""); 
      }
    
    } else {
        server.log("dev not found");
        server.log(request.body);
    }
    
    // send a response back saying everything was OK.
    response.send(200, "::)");
  } catch (ex) {
    server.log("err" + ex);

    response.send(500, "Internal Server Error: " + ex);
  }
}

function writeStatus(status) {
    statDev1 = status[0];
    statDev2 = status[1];
    statDev3 = status[2];
    statDev4 = status[3];
    statDev5 = status[4];
    statDev6 = status[5];
    server.log("### updated Device status :" + statDev1 + "," + statDev2 
        + ","+ statDev3 + ","+ statDev4 + ","+ statDev5 + ","+ statDev6);
        
    // server.log("IMP STATUS: " + status[0] + "," + status[1]+ "," + status[2]
    // + "," + status[3]+ "," + status[4]+ "," + status[5]);
    
    
}

device.on("status",writeStatus);

 
// register the HTTP handler
http.onrequest(requestHandler);

device.onconnect(function() {
    server.log("device connected to agent");
});
 
device.ondisconnect(function() {
    server.log("device disconnected from agent");
});
device.on("isOnline",function (notUsed) {
        server.log("!!!!!!!!!!!!!!!!!!!!device connected to agent");
    statOnline = 1;
});