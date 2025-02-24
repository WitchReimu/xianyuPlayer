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
  media_status_t resultStatus = AMediaExtractor_setDataSource(extractor, location);

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
  ALOGI("[%s]", __FUNCTION__);
  ssize_t inputIndex = AMediaCodec_dequeueInputBuffer(hwVideoDecoder, 2000);
  AVPacket *videoPacket = av_packet_alloc();
  uint8_t *inputBuffer = nullptr;
  int read_ret = av_read_frame(avFormatContext, videoPacket);

  if (read_ret < 0)
  {
	ALOGE("[%s] read frame error", __FUNCTION__);
	return;
  }

  if (videoPacket->stream_index == videoStreamIndex)
  {

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
	uint8_t *outputBuffer = nullptr;

	if (outputIndex > 0)
	{
	  size_t outputSize;
	  outputBuffer = AMediaCodec_getOutputBuffer(hwVideoDecoder, outputIndex, &outputSize);
	  AMediaCodec_releaseOutputBuffer(hwVideoDecoder, outputIndex, info.size != 0);
	}
	av_packet_free(&videoPacket);
  }
}

void HwMediacodecPlayer::testAMediacodecPlay(JNIEnv *env, jobject surface, const char *locationPath)
{
  AMediaExtractor *PExtractor = AMediaExtractor_new();
  ANativeWindow *PWindow = ANativeWindow_fromSurface(env, surface);
  size_t inputBufLength = 0;
  AMediaCodec *PCodec = nullptr;
  struct stat fileStat = {};
  ssize_t inputBufIndex = -1;
  ssize_t outputBufIndex = -1;
  stat(locationPath, &fileStat);
  FILE *PFile = fopen(locationPath, "r");
  int fileFd = fileno(PFile);
  AMediaExtractor_setDataSourceFd(PExtractor, fileFd, 0, fileStat.st_size);
  fclose(PFile);
  size_t trackCount = AMediaExtractor_getTrackCount(PExtractor);

  for (int i = 0; i < trackCount; ++i)
  {
	AMediaFormat *PFormat = AMediaExtractor_getTrackFormat(PExtractor, i);
	const char *formatStr = AMediaFormat_toString(PFormat);
	ALOGI("[%s] format %s", __FUNCTION__, formatStr);
	const char *mime;

	if (!AMediaFormat_getString(PFormat, AMEDIAFORMAT_KEY_MIME, &mime))
	{
	  ALOGE("[%s] format get mime failed ", __FUNCTION__);
	  return;
	} else if (strncmp(mime, "video/", 6) == 0)
	{
	  AMediaExtractor_selectTrack(PExtractor, i);
	  PCodec = AMediaCodec_createDecoderByType(mime);
	  AMediaCodec_configure(PCodec, PFormat, PWindow, nullptr, 0);
	  AMediaCodec_start(PCodec);
	  AMediaFormat_delete(PFormat);
	  break;
	}
	AMediaFormat_delete(PFormat);
  }

  openFFmpegcodec();
  AVPacket *videoPacket = av_packet_alloc();
  AVPacket *filterVideoPacket = av_packet_alloc();

  while (stop == 0)
  {
	inputBufIndex = AMediaCodec_dequeueInputBuffer(PCodec, 2000);
	if (inputBufIndex >= 0)
	{
	  getPacket(videoPacket, filterVideoPacket);
	  uint8_t *inputBuf = AMediaCodec_getInputBuffer(PCodec, inputBufIndex, &inputBufLength);
	  memcpy(inputBuf, filterVideoPacket->data, filterVideoPacket->size);
	  AMediaCodec_queueInputBuffer(PCodec,
								   inputBufIndex,
								   0,
								   filterVideoPacket->size,
								   filterVideoPacket->pts,
								   0);
	}
	av_packet_unref(filterVideoPacket);
	AMediaCodecBufferInfo info = {};
	outputBufIndex = AMediaCodec_dequeueOutputBuffer(PCodec, &info, 1000);

	if (outputBufIndex >= 0)
	{
	  size_t outputBufferLength = 0;
	  uint8_t *outputBuf = AMediaCodec_getOutputBuffer(PCodec, outputBufIndex, &outputBufferLength);
	  AMediaCodec_releaseOutputBuffer(PCodec, outputBufIndex, info.size != 0);
	}
  }
  av_packet_free(&videoPacket);
  av_packet_free(&filterVideoPacket);

  if (PCodec != nullptr)
  {
	AMediaCodec_delete(PCodec);
  }
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
