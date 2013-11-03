// configure the imp (best practice)
imp.configure("Smart Home", [], []);
 
// create a global variabled called led, 
// and assign pin9 to it
dev1 <- hardware.pin1;
dev2 <- hardware.pin2;
dev3 <- hardware.pin5;
dev4 <- hardware.pin7;
dev5 <- hardware.pin8;
dev6 <- hardware.pin9;
 
// configure led to be a digital output
dev1.configure(DIGITAL_OUT);
dev2.configure(DIGITAL_OUT);
dev3.configure(DIGITAL_OUT);
dev4.configure(DIGITAL_OUT);
dev5.configure(DIGITAL_OUT);
dev6.configure(DIGITAL_OUT);
 
function setDev1(dev1State) {
  server.log("Set dev1: " + dev1State);
  dev1.write(dev1State);
}

function setDev2(dev2State) {
  server.log("Set dev2: " + dev2State);
  dev2.write(dev2State);
  
}

function setDev3(dev3State) {
  server.log("Set dev3: " + dev3State);
  dev3.write(dev3State);
}

function setDev4(dev4State) {
  server.log("Set dev4: " + dev4State);
  dev4.write(dev4State);
}

function setDev5(dev5State) {
    server.log("Set dev5: " + dev5State);
    dev5.write(dev5State);
}

function setDev6(dev6State) {
    server.log("Set dev6: " + dev6State);
    dev6.write(dev6State);
}

function getStatus(notUsed) {
    agent.send("status", [dev1.read(), dev2.read(), dev3.read()  
                        , dev4.read(), dev5.read(), dev6.read()]);
}

 
 // register a handler for  messages from the agent
agent.on("dev1", setDev1);
agent.on("dev2", setDev2);
agent.on("dev3", setDev3);
agent.on("dev4", setDev4);
agent.on("dev5", setDev5);
agent.on("dev6", setDev6);
agent.on("status", getStatus);
agent.on("isOnline", function (notUsed) {
    agent.send("isOnline","")
});