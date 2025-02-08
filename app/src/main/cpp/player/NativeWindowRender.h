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
#include <semaphore>
#include <oboe/Oboe.h>

extern "C"
{
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libavutil/imgutils.h"
#include "libswscale/swscale.h"
#include "libavutil/time.h"
}

class NativeWindowRender
{
  public:
	NativeWindowRender(jobject nativeObject,
					   const char *url,
					   jobject surface,
					   JNIEnv *env);
	~NativeWindowRender();
	float speed = 1;
	void init();
	void play();
	void setDecodeState(int state);
	void changeNativeWindow(jobject surface, JNIEnv *env);
  private:
	uint skipFrame = 0;
	ANativeWindow *nativeWindow = nullptr;
	AVFormatContext *videoContext = nullptr;
	const AVCodec *videoDecode = nullptr;
	AVCodecContext *videoCodecContext = nullptr;
	AVRational videoRation = {};
	int dstWidth = 0;
	int dstHeight = 0;
	int streamIndex = 0;
	int decodeState = 0;
	std::binary_semaphore bSemaphore;
	char filePath[NAME_MAX] = {};
	SwsContext *swsContext = nullptr;
	std::thread *decodeThread = nullptr;
	AVFrame *renderFrame = nullptr;
	bool isAttach = false;
	ANativeWindow_Buffer nativeWindowBuffer;
	jobject callbackObject = nullptr;
	JavaVM *vm = nullptr;
	std::condition_variable conditionVar;
	std::mutex mutexDecodeLock;
	bool initVideoCodec();
	void setWindowBuffer();
	void allocRenderFrame();
	void initSwsContext();
	static void doDecode(NativeWindowRender *instance);
};

#endif //XIANYUPLAYER_APP_SRC_MAIN_CPP_PLAYER_NATIVEWINDOWRENDER_H
