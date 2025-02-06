//
// Created by wwwsh on 2025/1/28.
//

#include "NativeWindowRender.h"
#include "../CommonUtils.h"
#define TAG "NativeWindowRender"

NativeWindowRender::NativeWindowRender(jobject nativeObject,
									   const char *url,
									   jobject surface,
									   JNIEnv *env)
{
//  strcpy(filePath, url);
  strcpy(filePath, "/sdcard/Download/Tifa_Morning_Cowgirl_4K.mp4");
  nativeWindow = ANativeWindow_fromSurface(env, surface);
  callbackObject = env->NewGlobalRef(nativeObject);
  env->GetJavaVM(&vm);
}

NativeWindowRender::~NativeWindowRender()
{
  JNIEnv *env = getJniEnv(vm, isAttach);

  if (env != nullptr)
  {
	env->DeleteGlobalRef(callbackObject);

	if (isAttach)
	{
	  vm->DetachCurrentThread();
	}
  }
  ANativeWindow_release(nativeWindow);
}

void NativeWindowRender::play()
{
  if (decodeThread == nullptr)
  {
	decodeThread = new std::thread(doDecode, this);
  } else
  {
	ALOGW("[%s] decodeThread is inited", __FUNCTION__);
  }
}

bool NativeWindowRender::initVideoCodec()
{
  videoContext = avformat_alloc_context();
  int ret = avformat_open_input(&videoContext, filePath, nullptr, nullptr);

  if (ret != 0)
  {
	ALOGE("[%s] avformat open failed ", __FUNCTION__);
	return false;
  }
  ret = avformat_find_stream_info(videoContext, nullptr);

  if (ret < 0)
  {
	ALOGE("[%s]", __FUNCTION__);
	return false;
  }

  for (int i = 0; i < videoContext->max_streams; ++i)
  {
	AVStream *stream = videoContext->streams[i];

	if (stream->codecpar->codec_type == AVMEDIA_TYPE_VIDEO)
	{
	  streamIndex = i;
	  break;
	}
  }
  AVCodecParameters *codecParameters = videoContext->streams[streamIndex]->codecpar;
  videoDecode = avcodec_find_decoder(codecParameters->codec_id);
  videoCodecContext = avcodec_alloc_context3(videoDecode);
  avcodec_parameters_to_context(videoCodecContext, codecParameters);
  videoCodecContext->thread_count = 4;
  ret = avcodec_open2(videoCodecContext, videoDecode, nullptr);

  if (ret != 0)
  {
	ALOGE("[%s] avcodec_open failed ", __FUNCTION__);
	return false;
  }
  return true;
}

void NativeWindowRender::setWindowBuffer()
{
  int videoWidth = videoCodecContext->width;
  int videoHeight = videoCodecContext->height;
  int32_t windowWidth = ANativeWindow_getWidth(nativeWindow);
  int32_t windowHeight = ANativeWindow_getHeight(nativeWindow);
  float windowRatio = (float)windowWidth / (float)windowHeight;
  float videoRatio = (float)videoWidth / (float)videoHeight;

  if (windowRatio > videoRatio)
  {
	dstHeight = windowHeight;
	dstWidth = videoRatio * dstHeight;

  } else
  {
	dstWidth = windowWidth;
	dstHeight = videoHeight * dstWidth / videoWidth;
  }
  ANativeWindow_setBuffersGeometry(nativeWindow, dstWidth, dstHeight, WINDOW_FORMAT_RGBA_8888);
  if (callbackObject != nullptr)
  {
	JNIEnv *env = getJniEnv(vm, isAttach);
	jclass callbackClass = env->GetObjectClass(callbackObject);
	jmethodID methodId = env->GetMethodID(callbackClass, "notifyVideoResolution", "(II)V");
	env->CallVoidMethod(callbackObject, methodId, dstWidth, dstHeight);
  }
}

void NativeWindowRender::doDecode(NativeWindowRender *instance)
{
  int ret = 0;
  AVPacket *packet_p = av_packet_alloc();
  AVFrame *frame_p = av_frame_alloc();

  while (true)
  {
	while (instance->decodeState != 0)
	{
	  av_frame_free(&instance->renderFrame);
	  JNIEnv *env = getJniEnv(instance->vm, instance->isAttach);
	  if (env != nullptr)
	  {
		jclass callbackClass = env->GetObjectClass(instance->callbackObject);
		jmethodID callbackMethod = env->GetStaticMethodID(callbackClass,
														  "notifyPlayStatusChangeCallback",
														  "(I)V");
		env->CallStaticVoidMethod(callbackClass, callbackMethod, 5);

		if (instance->isAttach)
		  instance->vm->DetachCurrentThread();
	  }
	  instance->bSemaphore.acquire();
	}
	ret = av_read_frame(instance->videoContext, packet_p);

	if (ret < 0)
	{
	  ALOGE("[%s] read frame error info -> %s", __FUNCTION__, av_err2str(ret));
	  break;
	}

	if (packet_p->stream_index != instance->streamIndex)
	{
	  continue;
	}
	ret = avcodec_send_packet(instance->videoCodecContext, packet_p);

	if (ret < 0)
	{
	  av_packet_unref(packet_p);
	  ALOGE("[%s] send packet error info -> %s", __FUNCTION__, av_err2str(ret));
	  break;
	}
	ret = avcodec_receive_frame(instance->videoCodecContext, frame_p);

	if (ret == AVERROR(EAGAIN))
	{
	  av_packet_unref(packet_p);
	  av_frame_unref(frame_p);
	  continue;
	}

	if (ret < 0)
	{
	  ALOGE("[%s] receive frame error info -> %s", __FUNCTION__, av_err2str(ret));
	  break;
	}
	ret = sws_scale(instance->swsContext,
					frame_p->data,
					frame_p->linesize,
					0,
					instance->videoCodecContext->height,
					instance->renderFrame->data,
					instance->renderFrame->linesize);

	if (ret < 0)
	{
	  av_packet_unref(packet_p);
	  av_frame_unref(frame_p);
	  ALOGE("[%s] sws failed info-> %s", __FUNCTION__, av_err2str(ret));
	  break;
	}

	ANativeWindow_lock(instance->nativeWindow, &instance->nativeWindowBuffer, nullptr);
	uint8_t *dstBuffer = static_cast<uint8_t *>(instance->nativeWindowBuffer.bits);
	int srcLineSize = instance->dstWidth * 4;
	int dstLineSize = instance->nativeWindowBuffer.stride * 4;

	for (int i = 0; i < instance->dstHeight; ++i)
	{
	  memcpy(dstBuffer + i * dstLineSize,
			 instance->renderFrame->data[0] + i * srcLineSize,
			 srcLineSize);
	}
	ANativeWindow_unlockAndPost(instance->nativeWindow);
	av_packet_unref(packet_p);
	av_frame_unref(frame_p);
  }
  av_packet_free(&packet_p);
  av_frame_free(&frame_p);
  ALOGI("[%s] decode thread end ", __FUNCTION__);
}

void NativeWindowRender::init()
{
  initVideoCodec();
  setWindowBuffer();
  allocRenderFrame();
  initSwsContext();
  JNIEnv *env = getJniEnv(vm, isAttach);

  if (env != nullptr)
  {
	jclass callbackClass = env->GetObjectClass(callbackObject);
	jmethodID callbackMethod = env->GetStaticMethodID(callbackClass,
													  "notifyPlayStatusChangeCallback",
													  "(I)V");
	env->CallStaticVoidMethod(callbackClass, callbackMethod, oboe::StreamState::Open);

	if (isAttach)
	  vm->DetachCurrentThread();
  }
}

void NativeWindowRender::changeNativeWindow(jobject surface, JNIEnv *env)
{
  if (nativeWindow != nullptr)
  {
	ANativeWindow_release(nativeWindow);
	nativeWindow = ANativeWindow_fromSurface(env, surface);
	setWindowBuffer();
	allocRenderFrame();
	initSwsContext();
	decodeState = 0;
	bSemaphore.release();
  }
}

void NativeWindowRender::allocRenderFrame()
{
  renderFrame = av_frame_alloc();
  int bufferLength = av_image_get_buffer_size(AV_PIX_FMT_RGBA, dstWidth, dstHeight, 1);
  uint8_t *renderFrameBuffer = static_cast<uint8_t *>(av_mallocz(bufferLength));
  av_image_fill_arrays(renderFrame->data,
					   renderFrame->linesize,
					   renderFrameBuffer,
					   AV_PIX_FMT_RGBA,
					   dstWidth,
					   dstHeight,
					   1);
}

void NativeWindowRender::initSwsContext()
{
  swsContext = sws_getContext(videoCodecContext->width,
							  videoCodecContext->height,
							  videoCodecContext->pix_fmt,
							  dstWidth,
							  dstHeight,
							  AV_PIX_FMT_RGBA,
							  SWS_FAST_BILINEAR,
							  nullptr,
							  nullptr,
							  nullptr);
}

void NativeWindowRender::setDecodeState(int state)
{
  decodeState = state;
}
