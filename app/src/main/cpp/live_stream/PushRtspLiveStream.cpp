//
// Created by Administrator on 2025/3/30.
//

#include "PushRtspLiveStream.h"
#define TAG "PushRtspLiveStream"
#define MB 8388608

using std::string;
PushRtspLiveStream::PushRtspLiveStream(const char *outputUrl)
{
  strcpy(outputFileName, outputUrl);
  avFormat = avformat_alloc_context();
  int ret = avformat_alloc_output_context2(&avFormat, nullptr, "rtsp", outputFileName);

  if (ret < 0)
  {
	ALOGE("[%s] alloc output avformat failed ret value %d", __FUNCTION__, ret);
	ALOGE("[%s] error info %s", __FUNCTION__, av_err2str(ret));
	return;
  }
  videoOutputStream = avformat_new_stream(avFormat, nullptr);
  videoOutputStream->codecpar->codec_type = AVMEDIA_TYPE_VIDEO;
  videoOutputStream->codecpar->codec_id = AV_CODEC_ID_H264;
  videoOutputStream->codecpar->width = 1920;
  videoOutputStream->codecpar->height = 1080;


  /*videoEncode = avcodec_find_encoder(AV_CODEC_ID_H264);

  if (videoEncode == nullptr)
  {
	ALOGE("[%s] find encoder failed ", __FUNCTION__);
	return;
  }
  videoEncodeContext = avcodec_alloc_context3(videoEncode);
  videoEncodeContext->width = 1920;
  videoEncodeContext->height = 1080;
  videoEncodeContext->pix_fmt = AV_PIX_FMT_YUV420P;
  videoEncodeContext->bit_rate = 1 * MB;
  videoEncodeContext->rc_max_rate = 3 * MB;
  videoEncodeContext->time_base = {1, 60};
  ret = avcodec_open2(videoEncodeContext, videoEncode, nullptr);

  if (ret < 0)
  {
	ALOGE("[%s] open encode error code %d", __FUNCTION__, ret);
	ALOGE("[%s] error info %s", __FUNCTION__, av_err2str(ret));
	return;
  }
  videoOutputStream = avformat_new_stream(avFormat, videoEncode);

  if (videoOutputStream == nullptr)
  {
	ALOGE("[%s] new stream is error", __FUNCTION__);
	return;
  }
  videoOutputStream->codecpar->codec_tag = 0;
  avcodec_parameters_from_context(videoOutputStream->codecpar, videoEncodeContext);

  if (avFormat->oformat->flags & AVFMT_NOFILE)
  {
	//rtsp协议走的为tcp协议
	string url = string(outputFileName);
	unsigned long position = url.find(":");

	if (position != string::npos)
	{
	  url.replace(0, position, "tcp");
	}
	ALOGI("[%s] url %s", __FUNCTION__, url.c_str());
	ret = avio_open(&avFormat->pb, url.c_str(), AVIO_FLAG_WRITE);

	if (ret < 0)
	{
	  ALOGE("[%s] 打开avio失败", __FUNCTION__);
	  ALOGE("[%s] error info %s", __FUNCTION__, av_err2str(ret));
	  return;
	}
  }
  ret = avformat_write_header(avFormat, NULL);

  if (ret < 0)
  {
	ALOGE("[%s] write header failed,error info %s", __FUNCTION__, av_err2str(ret));
	return;
  }
  packet = av_packet_alloc();
  frame = av_frame_alloc();
  dstFrame = av_frame_alloc();*/
}

PushRtspLiveStream::~PushRtspLiveStream()
{
//  endPushRtspStream();
}

void PushRtspLiveStream::startPushRtspStream(unsigned char *planes[],
											 unsigned int planesSize[],
											 unsigned int arraySize,
											 unsigned int rowStrider,
											 unsigned int width,
											 unsigned int height)
{
  frame->width = width;
  frame->height = height;
  frame->format = AV_PIX_FMT_RGBA;
  dstFrame->width = videoEncodeContext->width;
  dstFrame->height = videoEncodeContext->height;
  dstFrame->format = videoEncodeContext->pix_fmt;
  av_frame_get_buffer(frame, 1);
  av_frame_get_buffer(dstFrame, 1);
  memcpy(frame->data[0], planes[0], planesSize[0]);

  if (swsContext == nullptr)
  {
	swsContext = sws_getContext(width,
								height,
								AV_PIX_FMT_RGBA,
								dstFrame->width,
								dstFrame->height,
								static_cast<AVPixelFormat>(dstFrame->format),
								SWS_FAST_BILINEAR,
								nullptr,
								nullptr,
								0);
  }
  int rescaleHeight = sws_scale(swsContext,
								frame->data,
								frame->linesize,
								0,
								frame->height,
								dstFrame->data,
								dstFrame->linesize);

  if (rescaleHeight <= 0)
  {
	ALOGE("[%s] sws_scale failed. error info %s", __FUNCTION__, av_err2str(rescaleHeight));
	return;
  }

  int ret = avcodec_send_frame(videoEncodeContext, dstFrame);

  if (ret != 0)
  {
	ALOGE("[%s] send frame failed %d", __FUNCTION__, ret);
	ALOGE("[%s] error info %s", __FUNCTION__, av_err2str(ret));
	return;
  }

  while (true)
  {
	ret = avcodec_receive_packet(videoEncodeContext, packet);

	if (ret != 0)
	{
	  ALOGE("[%s] receive packet failed error code %d", __FUNCTION__, ret);
	  ALOGE("[%s] error info %s", __FUNCTION__, av_err2str(ret));
	  av_frame_unref(dstFrame);
	  av_frame_unref(frame);
	  break;
	}
	av_packet_rescale_ts(packet, videoEncodeContext->time_base, videoOutputStream->time_base);
	av_interleaved_write_frame(avFormat, packet);
  }

  av_frame_unref(dstFrame);
  av_frame_unref(frame);
  av_packet_unref(packet);
}

void PushRtspLiveStream::queueInputBuffer()
{
  ALOGI("[%s] encode start ", __FUNCTION__);
  // 10. 模拟摄像头数据（实际应从Camera2 API获取）
  frame->format = videoEncodeContext->pix_fmt;
  frame->width = videoEncodeContext->width;
  frame->height = videoEncodeContext->height;
  av_frame_get_buffer(frame, 1);
  int height = videoEncodeContext->height;
  int width = videoEncodeContext->width;

  for (int i = 0; i < 100; i++)
  {  // 推流100帧
	// 填充YUV数据（示例用随机数据）
	for (int y = 0; y < height; y++)
	{
	  for (int x = 0; x < width; x++)
	  {
		frame->data[0][y * frame->linesize[0] + x] = rand() % 256;  // Y
	  }
	}
	for (int y = 0; y < height / 2; y++)
	{
	  for (int x = 0; x < width / 2; x++)
	  {
		frame->data[1][y * frame->linesize[1] + x] = rand() % 256;  // U
		frame->data[2][y * frame->linesize[2] + x] = rand() % 256;  // V
	  }
	}

	frame->pts = framePts;
	framePts += 1;

	// 编码帧
	int ret = avcodec_send_frame(videoEncodeContext, frame);
	if (ret == 0)
	{

	  while (true)
	  {
		ret = avcodec_receive_packet(videoEncodeContext, packet);

		if (ret == AVERROR(EAGAIN) || ret == AVERROR(EOF))
		{
		  break;
		}

		if (ret != 0)
		{
		  ALOGE("[%s] receive packet failed code %d. error info %s",
				__FUNCTION__,
				ret,
				av_err2str(ret));
		  break;
		}
		packet->stream_index = videoOutputStream->index;
		av_packet_rescale_ts(packet, videoEncodeContext->time_base, videoOutputStream->time_base);
		av_interleaved_write_frame(avFormat, packet);
		writePacketNumber += 1;
		ALOGI("[%s] packet number %d", __FUNCTION__, writePacketNumber);
		av_packet_unref(packet);
	  }
	}
  }
  av_frame_unref(frame);
  ALOGI("[%s] encode end ", __FUNCTION__);

  // 11. 写流尾部并释放资源
//  av_write_trailer(avFormat);
}

void PushRtspLiveStream::endPushRtspStream()
{
  av_write_trailer(avFormat);
  av_frame_free(&dstFrame);
  av_frame_free(&frame);
  av_packet_free(&packet);
  avcodec_free_context(&videoEncodeContext);
  if (avFormat && !(avFormat->oformat->flags & AVFMT_NOFILE))
  {
	avio_close(avFormat->pb);
  }
  avformat_free_context(avFormat);
}

void PushRtspLiveStream::setExtraData(uint8_t *data, int dataSize)
{
  AVCodecParameters *codecpar = videoOutputStream->codecpar;
  codecpar->extradata = static_cast<uint8_t *>(av_malloc(dataSize + AV_INPUT_BUFFER_PADDING_SIZE));
  memcpy(codecpar->extradata, data, dataSize);
  codecpar->extradata_size = dataSize;

  string targetUrl = string(outputFileName);
  unsigned long position = targetUrl.find(":");
  targetUrl.replace(0, position, "tcp");
  int ret = avio_open(&avFormat->pb, targetUrl.c_str(), AVIO_FLAG_WRITE);
  if (ret < 0)
  {
	ALOGE("[%s] avio 打开失败", __FUNCTION__);
	return;
  }
  avformat_write_header(avFormat, nullptr);
  packet = av_packet_alloc();
  frame = av_frame_alloc();
}

void PushRtspLiveStream::writeIntervalFrame(uint8_t *data,
											int dataSize,
											long ptsUs,
											bool isKeyFrame)
{
  if (basePts == 0)
	basePts = ptsUs;
  uint8_t tempdata[dataSize];
  memcpy(tempdata, data, dataSize);
  packet->stream_index = videoOutputStream->index;
  packet->data = tempdata;
  packet->size = dataSize;
  long srcPts = ptsUs - basePts;
  AVRational srcRational = {1, 1000000};
  packet->pts = av_rescale_q(srcPts, srcRational, videoOutputStream->time_base);
  if (isKeyFrame)
	packet->flags |= AV_PKT_FLAG_KEY;
  av_interleaved_write_frame(avFormat, packet);
  av_packet_unref(packet);
}

