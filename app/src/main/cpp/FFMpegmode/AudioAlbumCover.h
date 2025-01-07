//
// Created by Administrator on 2025/1/2.
//

#ifndef AUDIOALBUMCOVER_H
#define AUDIOALBUMCOVER_H

extern "C"
{
#include <libavformat/avformat.h>
}

#include <iostream>
#include <jni.h>
#include "../LogUtils.h"

class AudioAlbumCover
{
  public:
	AudioAlbumCover(const char *absolutePath, int length);
	~AudioAlbumCover();
	std::pair<int, char *> getAlbumCover();
  private:
	char path[NAME_MAX];
	AVFormatContext *formatContext = nullptr;

};

#endif //AUDIOALBUMCOVER_H
