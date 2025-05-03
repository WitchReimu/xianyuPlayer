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
#include "AvPacketMemoryManager.h"

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
	int readPacketState = Idle;
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
	int streamIndexInvalid = -1;
	char path[NAME_MAX] = {};
	AVFormatContext *formatContext = nullptr;
	int audioStreamIndex = streamIndexInvalid;
	int videoStreamIndex = streamIndexInvalid;
	const AVCodec *audioDecode = nullptr;
	const AVCodec *videoDecode = nullptr;
	std::thread *decodeThread = nullptr;
	std::thread *readPacketThread = nullptr;
	long lastTimeStamp = 0;
	long seekPosition = -1;
	struct AVRational audioStreamTimeBase{};
	jobject nativeBridge = nullptr;
	AVCodecContext *audioDecodeContext = nullptr;
	AVCodecContext *videoDecodeContext = nullptr;
	std::condition_variable decodeCon;
	std::condition_variable getPacketCon;
	std::condition_variable readPacketCon;
	std::mutex decodeMutex;
	std::mutex readPacketMutex;
	struct SwrContext *swrContext = nullptr;
	AVSampleFormat targetFmt = AV_SAMPLE_FMT_S16;
	JavaVM *vm = nullptr;
	int videoStreamNumber = 0;
	int audioStreamNumber = 0;
	AvPacketMemoryManager packetMemoryManager;
	static void doDecode(decodeStream *instance);
	static void doReadPacket(decodeStream *instance);
	int covertData(uint8_t *bufferData, AVFrame *frame_ptr, int bufferLength);
	bool initSwrContext();
	void setDecodeStatus(int status);

};

#endif //DECODESTREAM_H
