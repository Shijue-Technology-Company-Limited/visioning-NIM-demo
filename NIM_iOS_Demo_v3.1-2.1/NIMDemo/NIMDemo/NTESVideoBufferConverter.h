//
//  NTESVideoBufferConverter.h
//  NRTCDemo
//
//  Created by fenric on 16/10/10.
//  Copyright © 2016年 Netease. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreVideo/CVPixelBuffer.h>
#import <CoreMedia/CMSampleBuffer.h>

@interface NTESVideoBufferConverter : NSObject

//从 kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange 格式的 pixelBuffer 创建一个 kCVPixelFormatType_32BGRA 格式的 pixelBuffer
+ (CVPixelBufferRef)createBGRAPixelBufferFromNV12:(CVPixelBufferRef)pixelBuffer;

//从 kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange 格式的 sampleBuffer 创建一个 kCVPixelFormatType_32BGRA 格式的 sampleBuffer
+ (CMSampleBufferRef)createBGRASampleBufferFromNV12:(CMSampleBufferRef)sampleBuffer;

@end
