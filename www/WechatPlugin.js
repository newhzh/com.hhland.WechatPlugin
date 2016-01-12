/**
 * Created by hzh on 16/1/11.
 */
var exec = require('cordova/exec');

var WechatPlugin=function(){};

WechatPlugin.prototype.myAlert=function(){
    alert('i from plugin,updated');
};

WechatPlugin.prototype.echo=function(onSuccess,onError,message){
    var params=[message];
    exec(onSuccess,onError,'WechatPlugin','echo',[]);
};

module.exports=new WechatPlugin();
