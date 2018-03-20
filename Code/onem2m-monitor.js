var express = require('express');
var path = require('path');
var bodyParser = require('body-parser');
var request = require('request');
var app = express();

///////////////Parameters/////////////////
//CSE Params
var cseUri = "http://10.24.47.29:8080";

//AE params
var aeId = "Cae-monitor1";
var aeIp = "10.24.47.29";
var aePort = 3000;
var thresh = 200;
//////////////////////////////////////////

app.use(bodyParser.json());
app.listen(aePort, function () {
	console.log("AE Monitor listening on: "+aeIp+":"+aePort);
});
var avail = [true, true, true, true];
var reserve = [false, false, false, false];
var unlock = [false, false, false, false];
app.post('/', function (req, res) {
	console.log("\n◀◀◀◀◀")
	var content = req.body["m2m:sgn"].nev.rep["m2m:cin"].con;
	console.log("Receieved luminosity: "+content);
	content = content.split(',');
	var id = parseInt(content[0]) - 1;
	content = content[1];

	if(content>thresh != avail[id]){
		if(!reserve[id] && !unlock[id]){
			avail[id] = content>thresh;
			createContenInstance(id);
		}
		else{
			if(reserve[id] && unlock[id]){
				if(!avail[id]){
					reserve[id] = false;
					unlock[id] = false;
				}
				avail[id] = content>thresh;
				createContenInstance(id);
			}
		}
	}
	res.sendStatus(200);	
});

app.get('/list', function(req, res) {
	a = [];
	for(var i = 0; i < 4; i++){
		a[i] = avail[i] && !reserve[i] && !unlock[i]
	}
	res.json(a)
})
app.post('/reserve', function(req, res) {
	var id = req.body["id"]
	if(avail[id] && !reserve[id] && !unlock[id]){
		reserve[id] = true;
		createContenInstance(id);
		res.json(true);
	}
	else
		res.json(false);
})
app.post('/unlock', function(req, res) {
	var id = req.body["id"]
    if(avail[id] && reserve[id] && !unlock[id]){
    	unlock[id] = true;
    	createContenInstance(id);
    	res.json(true);
    }
    res.json(false);
})

createAE();
function createAE(){
	console.log("\n▶▶▶▶▶");
	var method = "POST";
	var uri= cseUri+"/server";
	var resourceType=2;
	var requestId = "123456";
	var representation = {
		"m2m:ae":{
			"rn":"mymonitor1",			
			"api":"app.company.com",
			"rr":"true",
			"poa":["http://"+aeIp+":"+aePort]
		}
	};

	console.log(method+" "+uri);
	console.log(representation);

	var options = {
		uri: uri,
		method: method,
		headers: {
			"X-M2M-Origin": aeId,
			"X-M2M-RI": requestId,
			"Content-Type": "application/json;ty="+resourceType
		},
		json: representation
	};

	request(options, function (error, response, body) {
		console.log("◀◀◀◀◀");
		if(error){
			console.log(error);
		}else{
			console.log(response.statusCode);
			console.log(body);
			createSubscription(1);
			createSubscription(2);
			createSubscription(3);
			createSubscription(4);
		}
	});
}


function createSubscription(dev){
	console.log("\n▶▶▶▶▶");
	var method = "POST";
	var uri= cseUri+"/server/mydevice" + dev + "/luminosity";
	var resourceType=23;
	var requestId = "123456" + dev;
	var representation = {
		"m2m:sub": {
			"rn": "subMonitor",
			"nu": ["Cae-monitor1"],
			"nct": 2,
			"enc": {
				"net": 3
			}
		}
	};

	console.log(method+" "+uri);
	console.log(representation);

	var options = {
		uri: uri,
		method: method,
		headers: {
			"X-M2M-Origin": aeId,
			"X-M2M-RI": requestId,
			"Content-Type": "application/json;ty="+resourceType
		},
		json: representation
	};

	request(options, function (error, response, body) {
		console.log("◀◀◀◀◀");
		if(error){
			console.log(error);
		}else{
			console.log(response.statusCode);
			console.log(body);
		}
	});
}

function createContenInstance(id){
	console.log("\n▶▶▶▶▶");
	var method = "POST";
	var uri= cseUri+"/server/mydevice" + (parseInt(id) + 1) + "/led";
	var resourceType=4;
	var requestId = "123456";
	var value = (avail[id] ? 1 : 0)  + ',' + (reserve[id] ? 1 : 0)  + ',' + (unlock[id] ? 1 : 0);
	var representation = {
		"m2m:cin":{
				"con": value
			}
		};

	console.log(method+" "+uri);
	console.log(representation);

	var options = {
		uri: uri,
		method: method,
		headers: {
			"X-M2M-Origin": aeId,
			"X-M2M-RI": requestId,
			"Content-Type": "application/json;ty="+resourceType
		},
		json: representation
	};

	request(options, function (error, response, body) {
		console.log("◀◀◀◀◀");
		if(error){
			console.log(error);
		}else{
			console.log(response.statusCode);
			console.log(body);
		}
	});
}


