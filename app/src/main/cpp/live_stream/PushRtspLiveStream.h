//
// Created by Administrator on 2025/3/30.
//

#ifndef XIANYUPLAYER_APP_SRC_MAIN_CPP_LIVE_STREAM_PUSHRTSPLIVESTREAM_H
#define XIANYUPLAYER_APP_SRC_MAIN_CPP_LIVE_STREAM_PUSHRTSPLIVESTREAM_H

extern "C"
{
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libswscale/swscale.h"
#include "libavutil/pixdesc.h"
}
#include "CommonUtils.h"
#include "CommonVideoFrameBuffer.h"
#include <string>

class PushRtspLiveStream
{
  public:
	PushRtspLiveStream(const char *outputUrl, int width, int height);
	~PushRtspLiveStream();
	void queueInputBuffer();
	void startPushRtspStream(unsigned char *planes[],
							 unsigned int planesSize[],
							 unsigned int arraySize,
							 unsigned int rowStrider,
							 unsigned int width,
							 unsigned int height);
	void endPushRtspStream();
	CommonVideoFrameBuffer *videoFrameBuffer = nullptr;
	void setExtraData(uint8_t *data, int dataSize);
	void writeIntervalFrame(uint8_t *data, int dataSize, long ptsUs, bool isKeyFrame);
  private:
	char outputFileName[NAME_MAX];
	const AVCodec *videoEncode = nullptr;
	AVFormatContext *avFormat = nullptr;
	AVCodecContext *videoEncodeContext = nullptr;
	AVStream *videoOutputStream = nullptr;
	AVFrame *frame = nullptr;
	AVPacket *packet = nullptr;
	SwsContext *swsContext = nullptr;
	AVFrame *dstFrame = nullptr;
	unsigned long framePts = 0;
	unsigned int writePacketNumber = 0;
	long basePts = 0;

};

#endif //XIANYUPLAYER_APP_SRC_MAIN_CPP_LIVE_STREAM_PUSHRTSPLIVESTREAM_H
