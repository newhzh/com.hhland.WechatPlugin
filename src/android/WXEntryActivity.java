package com.hhland.cordova.wx;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.hhland.cordova.wx.ShareWechatPlugin;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
	
	

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		ShareWechatPlugin.api.handleIntent(getIntent(), this);
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        ShareWechatPlugin.api.handleIntent(intent, this);
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
	        	ShareWechatPlugin.currentCallbackContext.success(ShareWechatPlugin.SUCCESS);
	            break;
	        case BaseResp.ErrCode.ERR_USER_CANCEL:
	        	ShareWechatPlugin.currentCallbackContext.error(ShareWechatPlugin.ERR_USER_CANCEL);
	            break;
	        case BaseResp.ErrCode.ERR_AUTH_DENIED:
	        	ShareWechatPlugin.currentCallbackContext.error(ShareWechatPlugin.ERR_AUTH_DENIED);
	            break;
	        case BaseResp.ErrCode.ERR_SENT_FAILED:
	        	ShareWechatPlugin.currentCallbackContext.error(ShareWechatPlugin.ERR_SENT_FAILED);
	            break;
	        case BaseResp.ErrCode.ERR_UNSUPPORT:
	        	ShareWechatPlugin.currentCallbackContext.error(ShareWechatPlugin.ERR_UNSUPPORT);
	            break;
	        case BaseResp.ErrCode.ERR_COMM:
	        	ShareWechatPlugin.currentCallbackContext.error(ShareWechatPlugin.ERR_COMM);
	            break;
	        default:
	        	ShareWechatPlugin.currentCallbackContext.error(ShareWechatPlugin.ERR_UNKNOWN);
	            break;
	    }
		finish();
	}
	
}
