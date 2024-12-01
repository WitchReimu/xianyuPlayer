//
// Created by Administrator on 2024/11/30.
//

#include "audioFrameQueue.h"

bool audioFrameQueue::isFull()
{
	int nextIndex = produceIndex + 1 % length;

	if (nextIndex == consumeIndex)
		return true;
	else
		return false;
}

bool audioFrameQueue::isEmpty()
{
	if (produceIndex == consumeIndex)
		return true;
	else
		return false;
}
void audioFrameQueue::resetAudioFrame(int resetIndex, audioFrame_t frame)
{
	audioFrame_t &audioFrame = frameQueue[resetIndex];

	if (audioFrame.buffer != nullptr)
	{
		av_free(audioFrame.buffer);
	}
	audioFrame = frame;
	produceIndex = (produceIndex + 1) % length;
}

const audioFrameQueue::audioFrame_t audioFrameQueue::getFrame()
{
	audioFrame_t frame = frameQueue[consumeIndex];
	return frame;
}

audioFrameQueue::audioFrameQueue()
{
	for (int i = 0; i < length; ++i)
	{
		struct audioFrame_t frame{};
		frameQueue.push_back(frame);
	}
}
void audioFrameQueue::resetDataLength(int resetIndex, int dataLength)
{
	audioFrame_t &frame = frameQueue[resetIndex];
	frame.dataLength = dataLength;
	produceIndex = (produceIndex + 1) % length;
}
