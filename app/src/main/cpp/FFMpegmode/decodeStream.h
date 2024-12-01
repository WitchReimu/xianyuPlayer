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

#include "../LogUtils.h"
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
	enum decodeState_enmu: int
	{
		Idle = -1,
		Inited = 0,
		Prepared = 1,
		Running = 2,
		Pause = 3,
		Stop = 4
	};
	audioFrameQueue queue;

	decodeStream(const char *path);
	int getDecodeFileSampleRate();
	int getDecodeFileChannelCount();
	~decodeStream();
	void initStream();
	void decodeFile();
private:
	char path[NAME_MAX] = {};
	AVFormatContext *formatContext = nullptr;
	int streamIndex = -1;
	const AVCodec *audioDecode = nullptr;
	std::thread *decodeThread = nullptr;
	AVCodecContext *audioDecodeContext = nullptr;
	int decodeState = Inited;
	std::condition_variable decodeCon;
	std::mutex decodeMutex;
	struct SwrContext *swrContext = nullptr;

	static void doDecode(decodeStream *instance);
	int covertData(uint8_t *bufferData, AVFrame *frame_ptr);
	bool initSwrContext();
};


#endif //DECODESTREAM_H
