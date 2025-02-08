//
// Created by Administrator on 2024/11/30.
//

#include "audioFrameQueue.h"
#define TAG "audioFrameQueue"

bool audioFrameQueue::isFull()
{
  int nextIndex = (produceIndex + 1) % length;

  if (nextIndex == consumeIndex)
	return true;
  else
	return false;
}

bool audioFrameQueue::isEmpty()
{
  if (produceIndex == consumeIndex)
  {
	ALOGW("[%s] 队列缓冲为空 ", __FUNCTION__);
	return true;
  } else
  {
	return false;
  }
}

bool audioFrameQueue::hasNext()
{
  if (produceIndex == consumeIndex + 1)
  {
	ALOGW("[%s] hasn't next frame", __FUNCTION__);
	return false;
  }
  return true;
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

void audioFrameQueue::resetDataLength(int resetIndex, int dataLength)
{
  audioFrame_t &frame = frameQueue[resetIndex];
  frame.dataLength = dataLength;
  produceIndex = (produceIndex + 1) % length;
}

audioFrameQueue::audioFrameQueue(int capacity)
{
  this->capacity = capacity;
  this->length = capacity + 1;

  for (int i = 0; i < length; ++i)
  {
	struct audioFrame_t frame{};
	frameQueue.push_back(frame);
  }
}

void audioFrameQueue::reset()
{
  for (auto &frame : frameQueue)
  {
	if (frame.buffer != nullptr)
	  av_free(frame.buffer);
	frame.buffer = nullptr;
	frame.bufferLength = 0;
	frame.dataLength = 0;
  }
}

