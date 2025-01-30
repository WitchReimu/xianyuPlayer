//
// Created by wwwsh on 2025/1/28.
//

#ifndef XIANYUPLAYER_APP_SRC_MAIN_CPP_PLAYER_NATIVEWINDOWRENDER_H
#define XIANYUPLAYER_APP_SRC_MAIN_CPP_PLAYER_NATIVEWINDOWRENDER_H

#include <android/native_window_jni.h>
#include <android/native_window.h>
#include <jni.h>
#include <iostream>
#include <thread>

extern "C"
{
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libavutil/imgutils.h"
#include "libswscale/swscale.h"
}

class NativeWindowRender
{
  public:
	NativeWindowRender(const char *url, jobject surface, JNIEnv *env);
	~NativeWindowRender();
	void play();
  private:
	ANativeWindow *nativeWindow = nullptr;
	AVFormatContext *videoContext = nullptr;
	const AVCodec *videoDecode = nullptr;
	AVCodecContext *videoCodecContext = nullptr;
	int dstWidth = 0;
	int dstHeight = 0;
	int streamIndex = 0;
	char filePath[NAME_MAX] = {};
	SwsContext *swsContext = nullptr;
	std::thread *decodeThread = nullptr;
	AVFrame *renderFrame = nullptr;
	ANativeWindow_Buffer nativeWindowBuffer;
	bool initVideoCodec();
	void setWindowBuffer();
	static void doDecode(NativeWindowRender *instance);
};

#endif //XIANYUPLAYER_APP_SRC_MAIN_CPP_PLAYER_NATIVEWINDOWRENDER_H
