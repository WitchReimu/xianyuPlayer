//
// Created by Administrator on 2025/3/9.
//

#include "VideoEncodeStream.h"
#include "CommonUtils.h"

#define TAG "VideoEncodeStream"

void log_error(const char *message, int error_code)
{
  char error_msg[AV_ERROR_MAX_STRING_SIZE];
  ALOGE("%s: %s\n", message, av_err2str(error_code));
}
// 将音频流与数据流编码为文件 示例
void VideoEncodeStream::transformTest()
{
  const char *input_filename = "/sdcard/Download/Tifa_Morning_Cowgirl_4K.mp4";
  const char *output_filename = "/sdcard/Download/Tifa_Morning_Cowgirl_H265_4K.mp4";
  AVFormatContext *input_ctx = NULL;
  AVFormatContext *output_ctx = NULL;
  AVCodecContext *video_decoder_ctx = NULL, *video_encoder_ctx = NULL;
  AVCodecContext *audio_decoder_ctx = NULL, *audio_encoder_ctx = NULL;
  AVStream *video_input_stream = NULL, *video_output_stream = NULL;
  AVStream *audio_input_stream = NULL, *audio_output_stream = NULL;
  const AVCodec *video_decoder = NULL, *audio_decoder = NULL;
  const AVCodec *audio_encoder = nullptr, *video_encoder = nullptr;
  int ret;
  AVPacket *packet = av_packet_alloc();
  AVFrame *frame = av_frame_alloc();

  // 打开输入文件
  if ((ret = avformat_open_input(&input_ctx, input_filename, NULL, NULL)) < 0)
  {
	log_error("Could not open input file", ret);
	goto cleanup;
  }
  if ((ret = avformat_find_stream_info(input_ctx, NULL)) < 0)
  {
	log_error("Could not find stream information", ret);
	goto cleanup;
  }

  // 查找视频和音频流
  for (int i = 0; i < input_ctx->nb_streams; i++)
  {
	if (input_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO && !video_input_stream)
	{
	  video_input_stream = input_ctx->streams[i];
	} else if (input_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO &&
			   !audio_input_stream)
	{
	  audio_input_stream = input_ctx->streams[i];
	}
  }
  if (!video_input_stream || !audio_input_stream)
  {
	ALOGE("Could not find video or audio stream\n");
	ret = AVERROR(EINVAL);
	goto cleanup;
  }

  // 创建解码器上下文
  video_decoder = avcodec_find_decoder(video_input_stream->codecpar->codec_id);
  if (!video_decoder)
  {
	ALOGE("Unsupported video codec\n");
	ret = AVERROR(EINVAL);
	goto cleanup;
  }
  video_decoder_ctx = avcodec_alloc_context3(video_decoder);
  if (!video_decoder_ctx)
  {
	ALOGE("Could not allocate video decoder context\n");
	ret = AVERROR(ENOMEM);
	goto cleanup;
  }
  if ((ret = avcodec_parameters_to_context(video_decoder_ctx, video_input_stream->codecpar)) < 0)
  {
	log_error("Could not copy video codec parameters", ret);
	goto cleanup;
  }
  if ((ret = avcodec_open2(video_decoder_ctx, video_decoder, NULL)) < 0)
  {
	log_error("Could not open video decoder", ret);
	goto cleanup;
  }

  audio_decoder = avcodec_find_decoder(audio_input_stream->codecpar->codec_id);
  if (!audio_decoder)
  {
	ALOGE("Unsupported audio codec\n");
	ret = AVERROR(EINVAL);
	goto cleanup;
  }
  audio_decoder_ctx = avcodec_alloc_context3(audio_decoder);
  if (!audio_decoder_ctx)
  {
	ALOGE("Could not allocate audio decoder context\n");
	ret = AVERROR(ENOMEM);
	goto cleanup;
  }
  if ((ret = avcodec_parameters_to_context(audio_decoder_ctx, audio_input_stream->codecpar)) < 0)
  {
	log_error("Could not copy audio codec parameters", ret);
	goto cleanup;
  }
  if ((ret = avcodec_open2(audio_decoder_ctx, audio_decoder, NULL)) < 0)
  {
	log_error("Could not open audio decoder", ret);
	goto cleanup;
  }

  // 创建输出文件
  if ((ret = avformat_alloc_output_context2(&output_ctx, NULL, NULL, output_filename)) < 0)
  {
	log_error("Could not create output context", ret);
	goto cleanup;
  }

  // 添加视频流
  video_output_stream = avformat_new_stream(output_ctx, NULL);
  if (!video_output_stream)
  {
	ALOGE("Could not create video stream\n");
	ret = AVERROR(ENOMEM);
	goto cleanup;
  }
  video_encoder = avcodec_find_encoder(AV_CODEC_ID_HEVC);
  if (!video_encoder)
  {
	ALOGE("Unsupported video encoder\n");
	ret = AVERROR(EINVAL);
	goto cleanup;
  }
  video_encoder_ctx = avcodec_alloc_context3(video_encoder);
  if (!video_encoder_ctx)
  {
	ALOGE("Could not allocate video encoder context\n");
	ret = AVERROR(ENOMEM);
	goto cleanup;
  }
  video_encoder_ctx->width = video_decoder_ctx->width;
  video_encoder_ctx->height = video_decoder_ctx->height;
  video_encoder_ctx->pix_fmt = video_decoder_ctx->pix_fmt;
  video_encoder_ctx->time_base = video_input_stream->time_base;
  if ((ret = avcodec_open2(video_encoder_ctx, video_encoder, NULL)) < 0)
  {
	log_error("Could not open video encoder", ret);
	goto cleanup;
  }
  if ((ret = avcodec_parameters_from_context(video_output_stream->codecpar, video_encoder_ctx)) < 0)
  {
	log_error("Could not copy video encoder parameters", ret);
	goto cleanup;
  }

  // 添加音频流
  audio_output_stream = avformat_new_stream(output_ctx, NULL);
  if (!audio_output_stream)
  {
	ALOGE("Could not create audio stream\n");
	ret = AVERROR(ENOMEM);
	goto cleanup;
  }
  audio_encoder = avcodec_find_encoder(AV_CODEC_ID_AAC);
  if (!audio_encoder)
  {
	ALOGE("Unsupported audio encoder\n");
	ret = AVERROR(EINVAL);
	goto cleanup;
  }
  audio_encoder_ctx = avcodec_alloc_context3(audio_encoder);
  if (!audio_encoder_ctx)
  {
	ALOGE("Could not allocate audio encoder context\n");
	ret = AVERROR(ENOMEM);
	goto cleanup;
  }
  audio_encoder_ctx->sample_rate = audio_decoder_ctx->sample_rate;
  audio_encoder_ctx->ch_layout = audio_decoder_ctx->ch_layout;
  audio_encoder_ctx->sample_fmt = audio_decoder_ctx->sample_fmt;
  audio_encoder_ctx->time_base = audio_decoder_ctx->time_base;
  if ((ret = avcodec_open2(audio_encoder_ctx, audio_encoder, NULL)) < 0)
  {
	log_error("Could not open audio encoder", ret);
	goto cleanup;
  }
  if ((ret = avcodec_parameters_from_context(audio_output_stream->codecpar, audio_encoder_ctx)) < 0)
  {
	log_error("Could not copy audio encoder parameters", ret);
	goto cleanup;
  }

  // 打开输出文件
  if (!(output_ctx->oformat->flags & AVFMT_NOFILE))
  {
	if ((ret = avio_open(&output_ctx->pb, output_filename, AVIO_FLAG_WRITE)) < 0)
	{
	  log_error("Could not open output file", ret);
	  goto cleanup;
	}
  }
  if ((ret = avformat_write_header(output_ctx, NULL)) < 0)
  {
	log_error("Could not write output header", ret);
	goto cleanup;
  }

  // 解码和编码循环
  if (!frame)
  {
	ALOGE("Could not allocate frame\n");
	ret = AVERROR(ENOMEM);
	goto cleanup;
  }
  while (1)
  {
	if ((ret = av_read_frame(input_ctx, packet)) < 0)
	{
	  break;
	}
	if (packet->stream_index == video_input_stream->index)
	{
	  // 解码视频帧
	  if ((ret = avcodec_send_packet(video_decoder_ctx, packet)) < 0)
	  {
		log_error("Error sending video packet to decoder", ret);
		goto cleanup;
	  }
	  while (ret >= 0)
	  {
		ret = avcodec_receive_frame(video_decoder_ctx, frame);
		if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
		{
		  break;
		} else if (ret < 0)
		{
		  log_error("Error receiving video frame from decoder", ret);
		  goto cleanup;
		}
		// 编码视频帧
		if ((ret = avcodec_send_frame(video_encoder_ctx, frame)) < 0)
		{
		  log_error("Error sending video frame to encoder", ret);
		  goto cleanup;
		}
		while (ret >= 0)
		{
		  ret = avcodec_receive_packet(video_encoder_ctx, packet);
		  if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
		  {
			break;
		  } else if (ret < 0)
		  {
			log_error("Error receiving video packet from encoder", ret);
			goto cleanup;
		  }
		  packet->stream_index = video_output_stream->index;
		  av_packet_rescale_ts(packet,
							   video_encoder_ctx->time_base,
							   video_output_stream->time_base);
		  if ((ret = av_interleaved_write_frame(output_ctx, packet)) < 0)
		  {
			log_error("Error writing video packet to output", ret);
			goto cleanup;
		  }
		}
	  }
	} else if (packet->stream_index == audio_input_stream->index)
	{
	  // 解码音频帧
	  if ((ret = avcodec_send_packet(audio_decoder_ctx, packet)) < 0)
	  {
		log_error("Error sending audio packet to decoder", ret);
		goto cleanup;
	  }
	  while (ret >= 0)
	  {
		ret = avcodec_receive_frame(audio_decoder_ctx, frame);
		if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
		{
		  break;
		} else if (ret < 0)
		{
		  log_error("Error receiving audio frame from decoder", ret);
		  goto cleanup;
		}
		// 编码音频帧
		if ((ret = avcodec_send_frame(audio_encoder_ctx, frame)) < 0)
		{
		  log_error("Error sending audio frame to encoder", ret);
		  goto cleanup;
		}
		while (ret >= 0)
		{
		  ret = avcodec_receive_packet(audio_encoder_ctx, packet);
		  if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
		  {
			break;
		  } else if (ret < 0)
		  {
			log_error("Error receiving audio packet from encoder", ret);
			goto cleanup;
		  }
		  packet->stream_index = audio_output_stream->index;
		  av_packet_rescale_ts(packet,
							   audio_encoder_ctx->time_base,
							   audio_output_stream->time_base);
		  if ((ret = av_interleaved_write_frame(output_ctx, packet)) < 0)
		  {
			log_error("Error writing audio packet to output", ret);
			goto cleanup;
		  }
		}
	  }
	}
	av_packet_unref(packet);
  }

  // 写入文件尾
  av_write_trailer(output_ctx);
  cleanup:
  if (frame)
	av_frame_free(&frame);
  if (video_decoder_ctx)
	avcodec_free_context(&video_decoder_ctx);
  if (audio_decoder_ctx)
	avcodec_free_context(&audio_decoder_ctx);
  if (video_encoder_ctx)
	avcodec_free_context(&video_encoder_ctx);
  if (audio_encoder_ctx)
	avcodec_free_context(&audio_encoder_ctx);
  if (input_ctx)
	avformat_close_input(&input_ctx);
  if (output_ctx && !(output_ctx->oformat->flags & AVFMT_NOFILE))
	avio_closep(&output_ctx->pb);
  if (output_ctx)
	avformat_free_context(output_ctx);
  ALOGI("[%s] 结束", __FUNCTION__);
  return;

}
