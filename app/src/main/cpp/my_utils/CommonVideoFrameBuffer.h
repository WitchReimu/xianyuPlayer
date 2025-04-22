//
// Created by Administrator on 2025/4/5.
//

#ifndef XIANYUPLAYER_APP_SRC_MAIN_CPP_MY_UTILS_COMMONVIDEOFRAMEBUFFER_H
#define XIANYUPLAYER_APP_SRC_MAIN_CPP_MY_UTILS_COMMONVIDEOFRAMEBUFFER_H

extern "C" {
#include "libavutil/pixfmt.h"
#include "libavutil/frame.h"
}
#include "CommonUtils.h"
#include <stdlib.h>
#include <vector>

class CommonVideoFrameBuffer
{
  private:
	unsigned int width = 0;
	unsigned int height = 0;
	int pixFormat = AV_PIX_FMT_YUV420P;
	int capacity = 0;
	int limit = 0;
	std::vector<AVFrame *> bufferQueue;

  public:
	CommonVideoFrameBuffer(int capacity, int width, int height, int pixFormat);
	~CommonVideoFrameBuffer();
};

#endif //XIANYUPLAYER_APP_SRC_MAIN_CPP_MY_UTILS_COMMONVIDEOFRAMEBUFFER_H
