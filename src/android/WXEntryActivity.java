package com.hhland.whip.wxapi;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import com.hhland.cordova.wx.ShareWechatPlugin;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		ShareWechatPlugin.instance.getWxAPI().handleIntent(getIntent(), this);
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        ShareWechatPlugin.instance.getWxAPI().handleIntent(intent, this);
    }
	
	@Override
	public void onReq(BaseReq arg0) {
		// TODO Auto-generated method stub
		// 微信发送请求到第三方应用时，会回调到该方法
		finish();
	}

	@Override
	public void onResp(BaseResp arg0) {
		// TODO Auto-generated method stub
		// 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
		switch (arg0.errCode) {
	        case BaseResp.ErrCode.ERR_OK:
	        	if (arg0.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
	        		authResp(arg0);
	        	}else{
	        		ShareWechatPlugin.instance.getCurrentCallbackContext().success(ShareWechatPlugin.SUCCESS);
	        	}
	            break;
	        case BaseResp.ErrCode.ERR_USER_CANCEL:
	        	ShareWechatPlugin.instance.getCurrentCallbackContext().error(ShareWechatPlugin.ERR_USER_CANCEL);
	            break;
	        case BaseResp.ErrCode.ERR_AUTH_DENIED:
	        	ShareWechatPlugin.instance.getCurrentCallbackContext().error(ShareWechatPlugin.ERR_AUTH_DENIED);
	            break;
	        case BaseResp.ErrCode.ERR_SENT_FAILED:
	        	ShareWechatPlugin.instance.getCurrentCallbackContext().error(ShareWechatPlugin.ERR_SENT_FAILED);
	            break;
	        case BaseResp.ErrCode.ERR_UNSUPPORT:
	        	ShareWechatPlugin.instance.getCurrentCallbackContext().error(ShareWechatPlugin.ERR_UNSUPPORT);
	            break;
	        case BaseResp.ErrCode.ERR_COMM:
	        	ShareWechatPlugin.instance.getCurrentCallbackContext().error(ShareWechatPlugin.ERR_COMM);
	            break;
	        default:
	        	ShareWechatPlugin.instance.getCurrentCallbackContext().error(ShareWechatPlugin.ERR_UNKNOWN);
	            break;
	    }
		finish();
	}
	
	protected void authResp(BaseResp resp) {
        SendAuth.Resp res = ((SendAuth.Resp) resp);
        JSONObject response = new JSONObject();
        try {
			response.put("code", res.code);
			response.put("state", res.state);
	        response.put("country", res.country);
	        response.put("lang", res.lang);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        ShareWechatPlugin.instance.getCurrentCallbackContext().success(response);
    }
	
}
