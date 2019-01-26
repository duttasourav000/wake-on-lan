var express = require('express');
var router = express.Router();

var status = false;
var lastCheckedTime = "";
// 1 minute
// 10 minute
// 30 minute
// 60 minute
// 120 minute
var pingFrequency = 10;
var pingFrequencies = {1: false, 10: true, 30: false, 60: false, 120: false};

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Internet', status: status, lastCheckedTime: lastCheckedTime, pingFrequencies: pingFrequencies });
});

router.get('/status-on', function(req, res, next) {
  status = true;
  res.send(getReturnData());
});

router.get('/status', function(req, res, next) {
  lastCheckedTime = Date().toString();
  res.send(getReturnData());
});

router.get('/status-off', function(req, res, next) {
  status = false;
  res.send(getReturnData());
});

router.get('/update-ping-freq', function(req, res, next) {
  console.log(pingFrequency, pingFrequencies);
  pingFrequencies[pingFrequency] = false;
  console.log(pingFrequency, pingFrequencies);
  pingFrequency = Number.parseInt(req.query.pingfreq);
  pingFrequencies[pingFrequency] = true;
  console.log(pingFrequency, pingFrequencies);
  res.send(getReturnData());
});

function getReturnData() {
  return {'status': status, 'ping-freq': pingFrequency, 'last-checked-time': lastCheckedTime}
}

module.exports = router;
