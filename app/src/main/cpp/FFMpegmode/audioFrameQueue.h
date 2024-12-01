//
// Created by Administrator on 2024/11/30.
//

#ifndef AUDIOFRAMEQUEUE_H
#define AUDIOFRAMEQUEUE_H

#include <vector>
#include "../LogUtils.h"

extern "C"
{
#include "libavutil/mem.h"
}


class audioFrameQueue
{
public:
	struct audioFrame_t
	{
		uint8_t *buffer;
		int dataLength;
		int bufferLength;
		audioFrame_t(uint8_t *data = nullptr, int dataLength = 0, int bufferLength = 0)
		{
			this->buffer = data;
			this->dataLength = dataLength;
			this->bufferLength = bufferLength;
		}
	};
	int consumeIndex = 0;
	int produceIndex = 0;
	int capacity = 2;
	int length = capacity + 1;
	std::vector<audioFrame_t> frameQueue;

	audioFrameQueue();
	const audioFrame_t getFrame();
	bool isFull();
	bool isEmpty();
	void resetDataLength(int resetIndex,int dataLength);
	void resetAudioFrame(int resetIndex, audioFrame_t frame);

	~audioFrameQueue()
	{

		for (const auto &frame : frameQueue)
		{
			av_free(frame.buffer);
		}
	}
private:

};


#endif //AUDIOFRAMEQUEUE_H
