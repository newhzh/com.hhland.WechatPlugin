//
//  CDVWechat.h
//  WechatPlugin
//
//  Created by 胡志华 on 16/1/20.
//  Copyright © 2016年 hhland. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>
#import "WXApi.h"

@interface CDVWechat : CDVPlugin <WXApiDelegate>

@property (nonatomic,strong) NSString *currentCallBackId;
//@property (nonatomic,strong) NSString *appId;

- (void)share:(CDVInvokedUrlCommand *)cmd;
- (void)haswx:(CDVInvokedUrlCommand *)cmd;
- (void)authRequest:(CDVInvokedUrlCommand *)cmd;
- (void)paymentRequest:(CDVInvokedUrlCommand *)cmd;

@end
