//
// Created by Administrator on 2025/4/5.
//

#include "CommonVideoFrameBuffer.h"
#define TAG "CommonVideoFrameBuffer"
CommonVideoFrameBuffer::CommonVideoFrameBuffer(int capacity, int width, int height, int pixFormat)
{
  this->width = width;
  this->height = height;
  this->pixFormat = pixFormat;
  AVFrame *frame = av_frame_alloc();
  frame->width = width;
  frame->height = height;
  frame->format = pixFormat;
  av_frame_get_buffer(frame, 1);
  ALOGI("[%s] linesize %d", __FUNCTION__, frame->linesize[0]);
  av_frame_free(&frame);
}

CommonVideoFrameBuffer::~CommonVideoFrameBuffer()
{

}
