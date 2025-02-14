//
// Created by toptech-6 on 2025/2/11.
//

#include "HwMediacodecPlayer.h"
#define TAG "HwMediacodecPlayer"

HwMediacodecPlayer::HwMediacodecPlayer(const char *location)
{
  strcpy(this->location, location);
}

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
}

void HwMediacodecPlayer::openFFmpegcodec()
{
  avFormatContext = avformat_alloc_context();
  const AVCodec *videoCodec = nullptr;
  int ret = avformat_open_input(&avFormatContext, location, nullptr, nullptr);

  if (ret != 0)
  {
	ALOGE("[%s] open input error inputPath %s", __FUNCTION__, location);
	return;
  }
  ret = avformat_find_stream_info(avFormatContext, nullptr);

  if (ret < 0)
  {
	ALOGE("[%s] find stream info error ", __FUNCTION__);
	return;
  }
  videoStreamIndex = av_find_best_stream(avFormatContext,
										 AVMEDIA_TYPE_VIDEO,
										 -1,
										 -1,
										 &videoCodec,
										 0);

  if (videoStreamIndex > 0)
  {
	ALOGE("[%s] find best stream", __FUNCTION__);
	return;
  }
  videoCodecContext = avcodec_alloc_context3(videoCodec);

  if (videoCodecContext == nullptr)
  {
	ALOGE("[%s] alloc avcodec error ", __FUNCTION__);
	return;
  }
  AVCodecParameters *videoParameter = avFormatContext->streams[videoStreamIndex]->codecpar;
  avcodec_parameters_to_context(videoCodecContext, videoParameter);
  ret = avcodec_open2(videoCodecContext, videoCodec, nullptr);

  if (ret < 0)
  {
	ALOGE("[%s] avcodec open failed ", __FUNCTION__);
	return;
  }
  videoBitStreamFilter = av_bsf_get_by_name("h264_mp4toannexb");
  ret = av_bsf_alloc(videoBitStreamFilter, &videoBsfCtx);

  if (ret < 0)
  {
	ALOGE("[%s] av bsf alloc error", __FUNCTION__);
	return;
  }
  ret = avcodec_parameters_copy(videoBsfCtx->par_in, videoParameter);

  if (ret < 0)
  {
	ALOGE("[%s] av parameters copy error", __FUNCTION__);
	return;
  }
  av_bsf_init(videoBsfCtx);
}

void HwMediacodecPlayer::startMediacodec()
{
  ssize_t inputIndex = AMediaCodec_dequeueInputBuffer(hwVideoDecoder, 2000);
  AVPacket *videoPacket = av_packet_alloc();
  uint8_t *inputBuffer = nullptr;

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
	inputBuffer = AMediaCodec_getInputBuffer(hwVideoDecoder, inputIndex, &inputSize);
  }
  int ret = av_bsf_send_packet(videoBsfCtx, videoPacket);

  if (ret != 0)
  {
	ALOGE("[%s] bsf send packet error ", __FUNCTION__);
	return;
  }
  ret = av_bsf_receive_packet(videoBsfCtx, videoPacket);

  if (ret != 0)
  {
	ALOGE("[%s] bsf receive packet error ", __FUNCTION__);
	return;
  }
  memcpy(inputBuffer, videoPacket->data, videoPacket->size);
  media_status_t resultStatus = AMediaCodec_queueInputBuffer(hwVideoDecoder,
															 inputIndex,
															 0,
															 videoPacket->size,
															 videoPacket->pts,
															 0);

  if (resultStatus != AMEDIA_OK)
  {
	ALOGE("[%s] enqueue the encoded data error ", __FUNCTION__);
	return;
  }
  av_packet_unref(videoPacket);
  struct AMediaCodecBufferInfo info = {};
  ssize_t outputIndex = AMediaCodec_dequeueOutputBuffer(hwVideoDecoder, &info, 1000);
}
