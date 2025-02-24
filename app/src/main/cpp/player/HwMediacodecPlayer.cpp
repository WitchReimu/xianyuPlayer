//
// Created by toptech-6 on 2025/2/11.
//

#include "HwMediacodecPlayer.h"
#include <android/asset_manager_jni.h>
#include <android/asset_manager.h>
#include <unistd.h>
#include <thread>
#define TAG "HwMediacodecPlayer"

HwMediacodecPlayer::HwMediacodecPlayer(JNIEnv *env, const char *location, jobject surface)
{
  strcpy(this->location, location);
  nativeWindow = ANativeWindow_fromSurface(env, surface);
}

void HwMediacodecPlayer::initMediacodec()
{
  extractor = AMediaExtractor_new();
  struct stat fileStat = {};
  stat(location, &fileStat);
  FILE *locationFile = fopen(location, "r");
  int fileFd = fileno(locationFile);
  media_status_t resultStatus = AMediaExtractor_setDataSourceFd(extractor,
																fileFd,
																0,
																fileStat.st_size);

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
	} else if (strncmp(mime, "video/", 6) == 0)
	{
	  AMediaExtractor_selectTrack(extractor, i);
	  hwVideoDecoder = AMediaCodec_createDecoderByType(mime);
	  AMediaCodec_configure(hwVideoDecoder, format, nativeWindow, nullptr, 0);
	  AMediaCodec_start(hwVideoDecoder);
	  AMediaFormat_delete(format);
	  break;
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
  AVPacket *videoPacket = av_packet_alloc();
  AVPacket *filterPacket = av_packet_alloc();
  uint8_t *inputBuffer = nullptr;

  while (hwVideoDecoder != nullptr)
  {
	ssize_t inputIndex = AMediaCodec_dequeueInputBuffer(hwVideoDecoder, 2000);

	if (inputIndex >= 0)
	{
	  size_t inputSize;
	  inputBuffer = AMediaCodec_getInputBuffer(hwVideoDecoder, inputIndex, &inputSize);
	  getPacket(videoPacket, filterPacket);
	  memcpy(inputBuffer, filterPacket->data, filterPacket->size);
	  media_status_t resultStatus = AMediaCodec_queueInputBuffer(hwVideoDecoder,
																 inputIndex,
																 0,
																 filterPacket->size,
																 filterPacket->pts,
																 0);

	  if (resultStatus != AMEDIA_OK)
	  {
		ALOGE("[%s] enqueue the encoded data error ", __FUNCTION__);
		return;
	  }
	}
	av_packet_unref(filterPacket);
	av_packet_unref(videoPacket);
	struct AMediaCodecBufferInfo info = {};
	ssize_t outputIndex = AMediaCodec_dequeueOutputBuffer(hwVideoDecoder, &info, 1000);
	uint8_t *outputBuffer = nullptr;

	if (outputIndex > 0)
	{
	  size_t outputSize;
	  outputBuffer = AMediaCodec_getOutputBuffer(hwVideoDecoder, outputIndex, &outputSize);
	  AMediaCodec_releaseOutputBuffer(hwVideoDecoder, outputIndex, info.size != 0);
	}
  }

  if (extractor != nullptr)
  {
	AMediaExtractor_delete(extractor);
  }

  if (hwVideoDecoder != nullptr)
  {
	AMediaCodec_delete(hwVideoDecoder);
  }
  av_packet_free(&videoPacket);
  av_packet_free(&filterPacket);
}

void HwMediacodecPlayer::getPacket(AVPacket *srcPacket, AVPacket *dstPacket)
{
  while (true)
  {
	int ret = av_read_frame(avFormatContext, srcPacket);

	if (ret < 0)
	{
	  ALOGE("[%s] ", __FUNCTION__);
	  stop = 1;
	  return;
	}

	if (srcPacket->stream_index != videoStreamIndex)
	{
	  continue;
	}
	ret = av_bsf_send_packet(videoBsfCtx, srcPacket);

	if (ret != 0)
	{
	  ALOGD("[%s] send packet failed error cause %s", __FUNCTION__, av_err2str(ret));
	  stop = 1;
	  return;
	}
	ret = av_bsf_receive_packet(videoBsfCtx, dstPacket);

	if (ret != 0)
	{
	  ALOGD("[%s] receive packet failed error cause %s", __FUNCTION__, av_err2str(ret));
	} else
	{
	  break;
	}
  }
}
