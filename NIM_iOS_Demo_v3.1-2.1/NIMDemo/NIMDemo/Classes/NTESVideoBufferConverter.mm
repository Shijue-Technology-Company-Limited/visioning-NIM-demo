//
//  NTESVideoBufferConverter.mm
//  NRTCDemo
//
//  Created by fenric on 16/10/10.
//  Copyright © 2016年 Netease. All rights reserved.
//

#import "NTESVideoBufferConverter.h"
#import "libyuv.h"


static void stillImageDataReleaseCallback(void *releaseRefCon, const void *baseAddress)
{
    free((void *)baseAddress);
}

@implementation NTESVideoBufferConverter

+ (CVPixelBufferRef)createBGRAPixelBufferFromNV12:(CVPixelBufferRef)pixelBuffer
{
    NSAssert(kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange == CVPixelBufferGetPixelFormatType(pixelBuffer),
             @"support nv12 video range only");
    
    CVPixelBufferLockBaseAddress(pixelBuffer, 0);
    
    uint8 *srcY = (uint8 *)CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 0);
    int srcStrideY = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 0);
    uint8* srcUV = (uint8*)CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 1);
    int srcStrideUV = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 1);
    size_t srcHeight = CVPixelBufferGetHeightOfPlane(pixelBuffer, 0);
    size_t srcWidth = CVPixelBufferGetWidthOfPlane(pixelBuffer, 0);

    size_t dstStride = srcStrideY * 4;
    uint8 *dstARGB = (uint8 *)calloc(1, srcHeight * dstStride);
    
    libyuv_nvs::NV12ToARGB(srcY, srcStrideY,
                           srcUV, srcStrideUV,
                           dstARGB, dstStride,
                           srcWidth, srcHeight);
    
    CVPixelBufferRef dstPixelbuffer = NULL;
    
    CVPixelBufferCreateWithBytes(kCFAllocatorDefault, srcWidth, srcHeight, kCVPixelFormatType_32BGRA, dstARGB, dstStride, stillImageDataReleaseCallback, NULL, NULL, &dstPixelbuffer);
        
    CVPixelBufferUnlockBaseAddress(pixelBuffer, 0);
    
    return dstPixelbuffer;
}

+ (CMSampleBufferRef)createBGRASampleBufferFromNV12:(CMSampleBufferRef)sampleBuffer
{
    CMSampleBufferRef sampleBufferBGRA = NULL;
    
    CVPixelBufferRef pixelBufferNV12 = CMSampleBufferGetImageBuffer(sampleBuffer);
    CVPixelBufferRef pixelBufferBGRA = [NTESVideoBufferConverter createBGRAPixelBufferFromNV12:pixelBufferNV12];
    if (pixelBufferBGRA) {
        
        CMTime frameTime = CMSampleBufferGetPresentationTimeStamp(sampleBuffer);
        CMSampleTimingInfo timing = {frameTime, frameTime, kCMTimeInvalid};
        CMVideoFormatDescriptionRef videoInfo = NULL;
        CMVideoFormatDescriptionCreateForImageBuffer(NULL, pixelBufferBGRA, &videoInfo);
        
        OSStatus status = CMSampleBufferCreateForImageBuffer(kCFAllocatorDefault, pixelBufferBGRA, true, NULL, NULL, videoInfo, &timing, &sampleBufferBGRA);
        if (status != noErr) {
            NSLog(@"Failed to create sample buffer with error %zd.", status);
        }
        
        CVPixelBufferRelease(pixelBufferBGRA);
        if(videoInfo) CFRelease(videoInfo);
    }
    return sampleBufferBGRA;
}

+ (CVPixelBufferRef)i420FrameToPixelBuffer:(NVSI420Frame *)i420Frame
{
        if (i420Frame == nil) {
                return NULL;
            }
    
        CVPixelBufferRef pixelBuffer = NULL;
    
    
        NSDictionary *pixelBufferAttributes = [NSDictionary dictionaryWithObjectsAndKeys:
                                                   [NSDictionary dictionary], (id)kCVPixelBufferIOSurfacePropertiesKey, nil];
    
        CVReturn result = CVPixelBufferCreate(kCFAllocatorDefault,
                                                                                                                        i420Frame.width,
                                                                                                                        i420Frame.height,
                                                                                                                        kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange,
                                                                                                                        (__bridge CFDictionaryRef)pixelBufferAttributes,
                                                                                                                        &pixelBuffer);
        
        if (result != kCVReturnSuccess) {
                NVSLogErr(@"Failed to create pixel buffer: %d", result);
                return NULL;
            }
    
    
        result = CVPixelBufferLockBaseAddress(pixelBuffer, 0);
        
        if (result != kCVReturnSuccess) {
                CFRelease(pixelBuffer);
                NVSLogErr(@"Failed to lock base address: %d", result);
                return NULL;
            }
    
    
        uint8 *dstY = (uint8 *)CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 0);
        int dstStrideY = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 0);
        uint8* dstUV = (uint8*)CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 1);
        int dstStrideUV = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 1);
    
        int ret = libyuv_nvs::I420ToNV12([i420Frame dataOfPlane:NVSI420FramePlaneY], [i420Frame strideOfPlane:NVSI420FramePlaneY],
                                                                                                     [i420Frame dataOfPlane:NVSI420FramePlaneU], [i420Frame strideOfPlane:NVSI420FramePlaneU],
                                                                                                     [i420Frame dataOfPlane:NVSI420FramePlaneV], [i420Frame strideOfPlane:NVSI420FramePlaneV],
                                                                                                     dstY, dstStrideY, dstUV, dstStrideUV,
                                                                                                     i420Frame.width, i420Frame.height);
        CVPixelBufferUnlockBaseAddress(pixelBuffer, 0);
        if (ret) {
                NVSLogErr(@"Error converting I420 VideoFrame to NV12: %d", result);
                CFRelease(pixelBuffer);
                return NULL;
            }
        
        return pixelBuffer;
}

@end
