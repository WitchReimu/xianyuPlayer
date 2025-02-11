//
// Created by toptech-6 on 2025/2/11.
//

#ifndef XIANYUPLAYER_APP_SRC_MAIN_CPP_PLAYER_HWMEDIACODECPLAYER_H
#define XIANYUPLAYER_APP_SRC_MAIN_CPP_PLAYER_HWMEDIACODECPLAYER_H

#include <media/NdkMediaExtractor.h>
#include <string>
#include "../CommonUtils.h"

extern "C"
{
#include "libavformat/avformat.h"
#include "libavcodec/bsf.h"
}

class HwMediacodecPlayer
{
  private:
	char location[NAME_MAX] = {};
	ANativeWindow *nativeWindow = nullptr;
	AMediaExtractor *extractor = nullptr;
	AMediaCodec *hwVideoDecoder = nullptr;
	void initMediacodec();
	void openFFmpegcodec(const char *localPath);
};

#endif //XIANYUPLAYER_APP_SRC_MAIN_CPP_PLAYER_HWMEDIACODECPLAYER_H
