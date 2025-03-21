//
// Created by Administrator on 2024/11/28.
//

#ifndef DECODESTREAM_H
#define DECODESTREAM_H

#include <limits.h>
#include <thread>
#include <vector>
#include <mutex>
#include <condition_variable>
#include <jni.h>

#include "CommonUtils.h"
#include "audioFrameQueue.h"

extern "C"
{
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
#include "libavcodec/avcodec.h"
}

class decodeStream
{
  public:
	enum decodeState_enum : int
	{
	  Idle = -1,
	  Initing = 0,
	  Prepared = 1,
	  Running = 2,
	  Pause = 3,
	  Stop = 4
	};
	int decodeState = Idle;
	audioFrameQueue queue = audioFrameQueue(10);

	decodeStream(const char *path, JNIEnv *env, jobject *nativeBridgeClass);
	~decodeStream();
	int getDecodeFileSampleRate();
	int getDecodeFileChannelCount();
	int getDecodeFileFormat();
	void initStream();
	void decodeFile();
	void notifyCond();
	int getDecodeState();
	int64_t getAudioDuration();
	bool seekToPosition(long position);
	void changeStream(const char *path);
	void openStream();
	void requestNextAudioFile();
	void requestRestartAudioFile();

  private:
	char path[NAME_MAX] = {};
	AVFormatContext *formatContext = nullptr;
	int streamIndex = -1;
	const AVCodec *audioDecode = nullptr;
	std::thread *decodeThread = nullptr;
	long lastTimeStamp = 0;
	long seekPosition = -1;
	struct AVRational audioStreamTimeBase{};
	jobject nativeBridge = nullptr;
	AVCodecContext *audioDecodeContext = nullptr;
	std::condition_variable decodeCon;
	std::mutex decodeMutex;
	struct SwrContext *swrContext = nullptr;
	AVSampleFormat targetFmt = AV_SAMPLE_FMT_S16;
	static void doDecode(decodeStream *instance);
	int covertData(uint8_t *bufferData, AVFrame *frame_ptr, int bufferLength);
	bool initSwrContext();
	JavaVM *vm = nullptr;
};

#endif //DECODESTREAM_H
