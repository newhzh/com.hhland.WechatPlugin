package com.hhland.WechatPlugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WechatPlugin extends CordovaPlugin{
	
	@Override
	public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("echo")) {
            String message = args.getString(0);
            this.echo(message, callbackContext);
            return true;
        }
        return false;
    }
	
	private void echo(String msg,CallbackContext callbackContext){
		if(msg!=null&&msg.length()>0){
			callbackContext.success(msg+",yes it is work!");
		}else{
			callbackContext.error("错误，传入参数为空！");
		}
	}
	
}
