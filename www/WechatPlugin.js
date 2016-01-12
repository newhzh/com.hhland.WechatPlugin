/**
 * Created by hzh on 16/1/11.
 */
var exec = require('cordova/exec');

var WechatPlugin=function(){};

WechatPlugin.prototype.myAlert=function(){
    alert('i from plugin');
};

WechatPlugin.prototype.echo=function(onSuccess,onError,message){
    var params=[message];
    exec(onSuccess,onError,'WechatPlugin','echo',params);
};

module.exports=new WechatPlugin();
