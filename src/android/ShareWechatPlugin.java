package com.hhland.cordova.wx;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.URLUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.webkit.URLUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.sdk.modelmsg.WXEmojiObject;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.modelmsg.SendAuth;

public class ShareWechatPlugin extends CordovaPlugin {
	
	public static final String ERR_WECHAT_NOT_INSTALLED = "1";
	public static final String ERR_INVALID_OPTIONS = "2";
	public static final String ERR_UNSUPPORTED_MEDIA_TYPE = "3";
	public static final String ERR_USER_CANCEL = "4";
	public static final String ERR_AUTH_DENIED = "5";
	public static final String ERR_SENT_FAILED = "6";
	public static final String ERR_UNSUPPORT = "7";
	public static final String ERR_COMM = "8";
	public static final String ERR_UNKNOWN = "9";
	public static final String SUCCESS = "0";
	
	private static final String TAG = "com.hhland.cordova.sharewechatplugin";
	private static final String WXAPPID_PROPERTY_KEY = "WECHATAPPID";
	private static final int THUMB_SIZE = 100;
	protected String appId;
	
	public static IWXAPI api;
	public static CallbackContext currentCallbackContext;
	
	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView){
		this.appId = preferences.getString(WXAPPID_PROPERTY_KEY, "");//it is work,skip this error info.
		this.api=WXAPIFactory.createWXAPI(webView.getContext(),this.appId,true);
		this.api.registerApp(this.appId);//将App注册到微信列表
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("echo")) {
            String message = args.getString(0);
            this.echo(message, callbackContext);
            return true;
        }else if(action.equals("share")){
        	try {
				this.share(args, callbackContext);
				return true;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				callbackContext.error("2|"+e.getMessage());			
			} catch (IOException e) {
				e.printStackTrace();
				callbackContext.error("2|"+e.getMessage());
			}
        }else if(action.equals("appid")){       	
        	callbackContext.success("got it:"+this.appId);
        	return true;
        }else if(action.equals("haswx")){
        	this.isWechatInstalled(callbackContext);
        	return true;
        }
        return false;
    }
	
	private void echo(String msg,CallbackContext callbackContext){
		if(msg!=null&&msg.length()>0){
			callbackContext.success(msg+",yes it is work!");
		}else{
			callbackContext.error("failed");
		}
	}
	
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
	
	 private void isWechatInstalled(CallbackContext callbackContext) {
		 if (!api.isWXAppInstalled()) {
			 callbackContext.error(ERR_WECHAT_NOT_INSTALLED);
		 }else{
			 callbackContext.success(SUCCESS);
		 }
		 currentCallbackContext = callbackContext;
	 }
	
	private void share(JSONArray params,final CallbackContext callbackContext) throws JSONException, MalformedURLException, IOException {
		//目前只支持 WXWebpageObject 分享
		//params[0] -- 分享的目标场景（0会话，1朋友圈，2微信收藏）
		//params[1] -- 分享内容的目标 url
		//params[2] -- 标题
		//params[3] -- 描述
		//params[4] -- 图片url
		//返回值：0-成功；1-微信未安装；2-发送失败
		//final IWXAPI api = ShareWechatPlugin.api;
		if (!api.isWXAppInstalled()) {
            callbackContext.error(ERR_WECHAT_NOT_INSTALLED);
            return;
		}
		if (params == null) {
            callbackContext.error(ERR_INVALID_OPTIONS);
            return;
        }
		
		int targetScene=params.getInt(0);
		String webUrl=params.getString(1);
		String title=params.getString(2);
		String description=params.getString(3);
		String imgUrl=params.getString(4);
		
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = webUrl;
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = title;
		msg.description = description;
		
		try{
			Bitmap bmp = BitmapFactory.decodeStream(new URL(imgUrl).openStream());
			Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
			bmp.recycle();
			msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
		}catch(Exception e){
			callbackContext.error(e.getMessage());
			return;
		}
		
		final SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;
		switch(targetScene){
		case 0:
			req.scene = SendMessageToWX.Req.WXSceneSession;
			break;
		case 1:
			req.scene = SendMessageToWX.Req.WXSceneTimeline;
			break;
		case 2:
			req.scene = SendMessageToWX.Req.WXSceneFavorite;
			break;
		default:
			req.scene = SendMessageToWX.Req.WXSceneTimeline;
		}
		
		// run in background
//        cordova.getThreadPool().execute(new Runnable() {
//            @Override
//            public void run() {
//                if (api.sendReq(req)) {
//                    Log.i(TAG, "wechat message sent successfully.");
//                    //send success
//                    callbackContext.success(NO_RESULT);
//                } else {
//                    Log.i(TAG, "wechat message sent failed.");
//                    // send error
//                    callbackContext.error(ERR_SENT_FAILED);
//                }
//            }
//        });
		
		try {
            boolean success = api.sendReq(req);
            if (!success) {
                callbackContext.error(ERR_SENT_FAILED);
                return;
            }else{
            	callbackContext.success(SUCCESS);
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
            return;
        }

        currentCallbackContext = callbackContext;
	}
}
