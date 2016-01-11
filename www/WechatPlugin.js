/**
 * Created by hzh on 16/1/11.
 */
var exec = require('cordova/exec');

var WechatPlugin=function(){};

WechatPlugin.prototype.myAlert=function(){
    alert('i from plugin');
};

module.exports=new WechatPlugin();
