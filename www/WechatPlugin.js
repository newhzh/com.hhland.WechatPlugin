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

WechatPlugin.prototype.share=function(onSuccess,onError,params){
    exec(onSuccess,onError,'WechatPlugin','share',params);
};

module.exports=new WechatPlugin();
