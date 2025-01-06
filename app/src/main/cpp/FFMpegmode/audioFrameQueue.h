//
// Created by Administrator on 2024/11/30.
//

#ifndef AUDIOFRAMEQUEUE_H
#define AUDIOFRAMEQUEUE_H

#include <vector>
#include "../LogUtils.h"
#include <oboe/Oboe.h>

extern "C"
{
#include "libavutil/mem.h"
}


class audioFrameQueue
{
public:
	/**
	 * buffer 数据缓冲区
	 * dataLength 数据缓冲区有多少个byte数据
	 * bufferLength 数据缓冲区可以缓冲多少个byte
	 */
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
	int capacity = 0;
	int length = 0;
	std::vector<audioFrame_t> frameQueue;

	audioFrameQueue(int capacity);
	bool isFull();
	bool isEmpty();
	void resetDataLength(int resetIndex,int dataLength);
	void resetAudioFrame(int resetIndex, audioFrame_t frame);
    void reset();

	~audioFrameQueue()
    {
        reset();
        frameQueue.clear();
    }
private:
};


#endif //AUDIOFRAMEQUEUE_H
