/**
 * Created by hzh on 16/1/11.
 */
var exec = require('cordova/exec');

var WechatPlugin=function(){};

WechatPlugin.prototype.myAlert=function(){
    alert('i from plugin,updated');
};

WechatPlugin.prototype.mycheck=function(onSuccess,onError,message){
    var params=[message];
    exec(onSuccess,onError,'WechatPlugin','echo',params);
};

WechatPlugin.prototype.myshare=function(onSuccess,onError,params){
    exec(onSuccess,onError,'WechatPlugin','share',params);
};

WechatPlugin.prototype.getAppId=function(onSuccess,onError){
    exec(onSuccess,onError,'WechatPlugin','appid',[]);
};

WechatPlugin.prototype.checkWechatInstalled= function (onSuccess,onError) {
    exec(onSuccess,onError,'WechatPlugin','haswx',[]);
};

WechatPlugin.prototype.sendAuth= function (onSuccess,onError,params) {
    exec(onSuccess,onError,'WechatPlugin','authRequest',params);
};

WechatPlugin.prototype.sendPayment= function (onSuccess,onError,params) {
    exec(onSuccess,onError,'WechatPlugin','paymentRequest',params);
};

module.exports=new WechatPlugin();
