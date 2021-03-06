//
//  CDVWechat.m
//  WechatPlugin
//
//  Created by 胡志华 on 16/1/20.
//  Copyright © 2016年 hhland. All rights reserved.
//

#import "CDVWechat.h"

@implementation CDVWechat

static int const THUMB_SIZE=320;

NSString *WXAPPID_PROPERTY_KEY = @"wechatappid";
NSString *ERR_WECHAT_NOT_INSTALLED = @"1";
NSString *ERR_INVALID_OPTIONS = @"2";
NSString *ERR_UNSUPPORTED_MEDIA_TYPE = @"3";
NSString *ERR_USER_CANCEL = @"4";
NSString *ERR_AUTH_DENIED = @"5";
NSString *ERR_SENT_FAILED = @"6";
NSString *ERR_UNSUPPORT = @"7";
NSString *ERR_COMM = @"8";
NSString *ERR_UNKNOWN = @"9";
NSString *SUCCESS = @"0";

- (void)pluginInitialize{
    self.appId = [[self.commandDelegate settings] objectForKey:WXAPPID_PROPERTY_KEY];
    BOOL success = [WXApi registerApp:self.appId];
    NSLog(@"appid:%@",self.appId);
    NSLog(@"reg result:%@",success?@"注册成功":@"注册失败");
}

- (void)haswx:(CDVInvokedUrlCommand *)cmd{
    CDVPluginResult *result=nil;
    if ([WXApi isWXAppInstalled]) {
        //已安装微信客户端
        result=[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:SUCCESS];
        [self.commandDelegate sendPluginResult:result callbackId:cmd.callbackId];
    }else{
        //未安装微信客户端
        result=[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_WECHAT_NOT_INSTALLED];
        [self.commandDelegate sendPluginResult:result callbackId:cmd.callbackId];
    }
}

- (void)share:(CDVInvokedUrlCommand *)cmd{
    CDVPluginResult *result=nil;
    
    //判断微信app是否已安装
    if (![WXApi isWXAppInstalled]) {
        result=[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_WECHAT_NOT_INSTALLED];
        [self.commandDelegate sendPluginResult:result callbackId:cmd.callbackId];
        return;
    }
    
    //判断参数是否合法
    NSArray *params=cmd.arguments;
    if (!params) {
        result=[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_INVALID_OPTIONS];
        [self.commandDelegate sendPluginResult:result callbackId:cmd.callbackId];
        return;
    }
    
    self.currentCallBackId = cmd.callbackId;
    
    //目前只支持 WXWebpageObject 分享
    //params[0] -- 分享的目标场景（0会话，1朋友圈，2微信收藏）
    //params[1] -- 分享内容的目标 url
    //params[2] -- 标题
    //params[3] -- 描述
    //params[4] -- 图片url
    SendMessageToWXReq *request=[SendMessageToWXReq new];
    request.bText =NO;
    NSInteger sceneParam=[[params objectAtIndex:0] integerValue];
    switch (sceneParam) {
        case 0:
            request.scene=WXSceneSession;
            break;
        case 1:
            request.scene=WXSceneTimeline;
            break;
        case 2:
            request.scene=WXSceneFavorite;
            break;
        default:
            request.scene=WXSceneSession;
            break;
    }
    NSString *title=[params objectAtIndex:2];
    NSString *description=[params objectAtIndex:3];
    UIImage *img =[self getUIImageFromURL:[params objectAtIndex:4]];
    WXWebpageObject *webObj=[WXWebpageObject object];
    webObj.webpageUrl=[params objectAtIndex:1];
    
    WXMediaMessage *msg=[WXMediaMessage message];
    [msg setTitle:title];
    [msg setDescription:description];
    [msg setMediaObject:webObj];
    [msg setThumbImage:img];
    
    request.message = msg;
    BOOL sendSuccess = [WXApi sendReq:request];
    if (!sendSuccess) {
        result=[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_SENT_FAILED];
        [self.commandDelegate sendPluginResult:result callbackId:cmd.callbackId];
        self.currentCallBackId = nil;
    }else{
        result=[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:SUCCESS];
        [self.commandDelegate sendPluginResult:result callbackId:cmd.callbackId];
    }
}

- (UIImage *)getUIImageFromURL:(NSString *)url{
    NSData *data = [NSData dataWithContentsOfURL:[NSURL URLWithString:url]];
    UIImage *image = [UIImage imageWithData:data];
    
    if (image.size.width > THUMB_SIZE || image.size.height > THUMB_SIZE){
        CGFloat width = 0;
        CGFloat height = 0;
        if (image.size.width > image.size.height){
            width = THUMB_SIZE;
            height = width * image.size.height / image.size.width;
        }else{
            height = THUMB_SIZE;
            width = height * image.size.width / image.size.height;
        }
        //裁剪
        UIGraphicsBeginImageContext(CGSizeMake(width, height));
        [image drawInRect:CGRectMake(0, 0, width, height)];
        UIImage *scaledImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        
        return scaledImage;
    }
    
    return image;
}

//微信授权登录请求
- (void)authRequest:(CDVInvokedUrlCommand *)cmd{
    CDVPluginResult *result=nil;
    
//    NSLog(@"plugin － 开始");
    
    //判断微信app是否已安装
    if (![WXApi isWXAppInstalled]) {
        result=[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_WECHAT_NOT_INSTALLED];
        [self.commandDelegate sendPluginResult:result callbackId:cmd.callbackId];
        return;
    }
//    NSLog(@"plugin － 微信客户端已安装");
    
    //判断参数是否合法
    NSArray *params=cmd.arguments;
    if (!params || [params count]<2) {
        result=[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_INVALID_OPTIONS];
        [self.commandDelegate sendPluginResult:result callbackId:cmd.callbackId];
        return;
    }
//    NSLog(@"plugin － 参数正确");
    
    //参数：appid,scope,state
    NSString *scope=[params objectAtIndex:0];
    NSString *state=[params objectAtIndex:1];
    SendAuthReq* req =[[SendAuthReq alloc] init];
    req.scope=scope;
    req.state=state;
    
    BOOL sendSuccess = [WXApi sendReq:req];
    if(sendSuccess){
        self.currentCallBackId = cmd.callbackId;
        NSLog(@"plugin － 授权请求发送成功");
    }else{
        NSLog(@"plugin － 授权请求发送失败");
    }
//    if (!sendSuccess) {
//        result=[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_SENT_FAILED];
//        [self.commandDelegate sendPluginResult:result callbackId:cmd.callbackId];
//        self.currentCallBackId = nil;
//    }else{
//        result=[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:SUCCESS];
//        [self.commandDelegate sendPluginResult:result callbackId:cmd.callbackId];
//    }
    
}

//微信支付请求
- (void)paymentRequest:(CDVInvokedUrlCommand *)cmd{
    CDVPluginResult *result=nil;
    
    //判断微信app是否已安装
    if (![WXApi isWXAppInstalled]) {
        result=[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_WECHAT_NOT_INSTALLED];
        [self.commandDelegate sendPluginResult:result callbackId:cmd.callbackId];
        return;
    }
    
    //判断参数是否合法
    NSArray *params=cmd.arguments;
    if (!params || [params count]<5) {
        result=[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_INVALID_OPTIONS];
        [self.commandDelegate sendPluginResult:result callbackId:cmd.callbackId];
        return;
    }
    
    //参数，传入时按顺序（partnerId，prepayId，timeStamp，nonceStr，sign）
    PayReq *req = [[PayReq alloc] init];
    req.partnerId = [params objectAtIndex:0];
    req.prepayId = [params objectAtIndex:1];
    req.timeStamp = [[params objectAtIndex:2] intValue];
    req.nonceStr = [params objectAtIndex:3];
    req.package = @"Sign=WXPay";
    req.sign = [params objectAtIndex:4];
    
    BOOL sendSuccess = [WXApi sendReq:req];
    if(sendSuccess){
        self.currentCallBackId = cmd.callbackId;
        NSLog(@"微信支付请求发送成功");
    }else{
        NSLog(@"微信支付请求发送失败!");
    }
    
//    if (!sendSuccess) {
//        result=[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_SENT_FAILED];
//        [self.commandDelegate sendPluginResult:result callbackId:cmd.callbackId];
//        self.currentCallBackId = nil;
//    }else{
//        self.currentCallBackId = cmd.callbackId;
//        result=[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:SUCCESS];
//        [self.commandDelegate sendPluginResult:result callbackId:cmd.callbackId];
//    }
}

#pragma mark - WXApiDelegate
- (void)onReq:(BaseReq *)req{
    //收到一个来自微信的请求，第三方应用程序处理完后调用sendResp向微信发送结果
}

- (void)onResp:(BaseResp *)resp{
    //发送一个sendReq后，收到微信的回应
//    NSLog(@"plugin － 得到反馈");
    if(!self.currentCallBackId){
        return;
    }
    
    BOOL success=NO;
    CDVPluginResult *result=nil;
    switch (resp.errCode) {
        case WXSuccess:
            success=YES;
            break;
        case WXErrCodeUserCancel:
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_USER_CANCEL];
            break;
        case WXErrCodeSentFail:
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_SENT_FAILED];
            break;
        case WXErrCodeAuthDeny:
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_AUTH_DENIED];
            break;
        case WXErrCodeUnsupport:
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_UNSUPPORT];
            break;
        case WXErrCodeCommon:
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_COMM];
            break;
        default:
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:ERR_UNKNOWN];
            break;
    }
    
    if(success){
        if ([resp isKindOfClass:[SendMessageToWXResp class]]) {
            //分享回调
            NSLog(@"plugin － 分享回调");
            result=[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:SUCCESS];
        }else if([resp isKindOfClass:[SendAuthResp class]]){
            //授权登录请求的回调
            NSLog(@"plugin － 授权回调");
            NSDictionary *response = nil;
            SendAuthResp* authResp = (SendAuthResp*)resp;
            response = @{
                         @"errCode": [NSString stringWithFormat:@"%d",authResp.errCode],
                         @"code": authResp.code != nil ? authResp.code : @"",
                         @"state": authResp.state != nil ? authResp.state : @"",
                         @"lang": authResp.lang != nil ? authResp.lang : @"",
                         @"country": authResp.country != nil ? authResp.country : @"",
                         };
            result=[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:response];
        }else{
            result=[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:SUCCESS];
        }
    }
    
    [self.commandDelegate sendPluginResult:result callbackId:self.currentCallBackId];
    self.currentCallBackId=nil;
}

-(void)handleOpenURL:(NSNotification *)notification{
    NSURL* url = [notification object];
    if ([url isKindOfClass:[NSURL class]] && [url.scheme isEqualToString:self.appId])
    {
        [WXApi handleOpenURL:url delegate:self];
    }
}

@end
