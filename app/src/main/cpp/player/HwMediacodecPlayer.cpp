//
// Created by toptech-6 on 2025/2/11.
//

#include "HwMediacodecPlayer.h"
#define TAG "HwMediacodecPlayer"

void HwMediacodecPlayer::initMediacodec()
{
  extractor = AMediaExtractor_new();
  media_status_t resultStatus = AMediaExtractor_setDataSource(extractor,
															  "/sdcard/Download/Tifa_Morning_Cowgirl_4K.mp4");
  //  AMediaExtractor_setDataSource(extractor,location);

  if (resultStatus != AMEDIA_OK)
  {
	ALOGE("[%s] Error setting extractor data source, err %d ", __FUNCTION__, resultStatus);
	return;
  }
  size_t trackCount = AMediaExtractor_getTrackCount(extractor);

  for (int i = 0; i < trackCount; ++i)
  {
	AMediaFormat *format = AMediaExtractor_getTrackFormat(extractor, i);
	const char *formatString = AMediaFormat_toString(format);
	ALOGI("[%s] track %d format: %s", __FUNCTION__, i, formatString);
	const char *mime;

	if (!AMediaFormat_getString(format, AMEDIAFORMAT_KEY_MIME, &mime))
	{
	  ALOGE("[%s] no mime type ", __FUNCTION__);
	  return;
	} else if (strncmp(mime, "video/", 6))
	{
	  AMediaExtractor_selectTrack(extractor, i);
	  hwVideoDecoder = AMediaCodec_createDecoderByType(mime);
	  AMediaCodec_configure(hwVideoDecoder, format, nativeWindow, nullptr, 0);
	  AMediaCodec_start(hwVideoDecoder);
	}
	AMediaFormat_delete(format);
  }
  ssize_t inputIndex = AMediaCodec_dequeueInputBuffer(hwVideoDecoder, 2000);

  if (inputIndex < 0)
  {

	if (inputIndex == AMEDIACODEC_INFO_TRY_AGAIN_LATER)
	{
	  ALOGW("[%s] get input buffer index TRY_AGAIN_LATER", __FUNCTION__);
	} else
	{
	  ALOGE("[%s] Failed to get input type", __FUNCTION__);
	  return;
	}
  } else
  {
	size_t inputSize;
	uint8_t *inputBuffer = AMediaCodec_getInputBuffer(hwVideoDecoder, inputIndex, &inputSize);
  }
  resultStatus = AMediaCodec_queueInputBuffer(hwVideoDecoder, inputIndex, 0, 0, 0, 0);

  if (resultStatus == AMEDIA_OK)
  {

  }
}

void HwMediacodecPlayer::openFFmpegcodec(const char *localPath)
{
  strcpy(this->location, localPath);

}
