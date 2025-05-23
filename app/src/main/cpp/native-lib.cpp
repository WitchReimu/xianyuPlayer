#include <jni.h>
#include <string>
#include "VideoEncodeStream.h"
#include "fileMetaDataInfo.h"
#include "decodeStream.h"
#include "oboePlayer.h"
#include "AudioAlbumCover.h"
#include "NativeWindowRender.h"
#include "HwMediacodecPlayer.h"
#include "PushRtspLiveStream.h"
#define TAG "native-lib"

extern "C"
{

jobjectArray getMetadata(JNIEnv *env, jobject thiz, jstring file_path)
{
  const char *filePath = env->GetStringUTFChars(file_path, nullptr);
  fileMetaDataInfo *info = new fileMetaDataInfo(filePath);
  std::vector<std::shared_ptr<const AVDictionaryEntry>> dictionEntry = info->getMetaData();
  jclass dataClass = env->FindClass("com/example/xianyuplayer/database/MusicMetadata");
  jmethodID dataInitMethod =
	  env->GetMethodID(dataClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
  jobjectArray dataArray = env->NewObjectArray(dictionEntry.size(), dataClass, nullptr);

  for (int i = 0; i < dictionEntry.size(); ++i)
  {
	std::shared_ptr<const AVDictionaryEntry> item = dictionEntry[i];
	jstring jkey = env->NewStringUTF(item->key);
	jstring jvalue = env->NewStringUTF(item->value);
	jobject dataElement = env->NewObject(dataClass, dataInitMethod, jkey, jvalue);
	env->SetObjectArrayElement(dataArray, i, dataElement);
  }
  env->ReleaseStringUTFChars(file_path, filePath);
  return dataArray;
}

jlong openDecodeStream(JNIEnv *env,
					   jobject natvieClass,
					   jstring path,
					   jlong streamPtr,
					   jlong playerPtr)
{
  const char *filePath = env->GetStringUTFChars(path, nullptr);
  decodeStream *decoder_ptr = nullptr;

  if (streamPtr == 0)
  {
	decoder_ptr = new decodeStream(filePath, env, &natvieClass);
  } else if (playerPtr != 0)
  {
	oboePlayer *player_ptr = reinterpret_cast<oboePlayer *>(playerPtr);
	player_ptr->closePlay();
	decoder_ptr = reinterpret_cast<decodeStream *>(streamPtr);
	decoder_ptr->changeStream(filePath);
  }
  env->ReleaseStringUTFChars(path, filePath);
  return reinterpret_cast<jlong>(decoder_ptr);
}

void startDecodeStream(JNIEnv *env, jobject activity, jlong streamPtr)
{
  if (streamPtr != 0)
  {
	decodeStream *decoder_ptr = reinterpret_cast<decodeStream *>(streamPtr);
	decoder_ptr->decodeFile();
  }
}

jlong initPlay(JNIEnv *env, jobject activity, jlong streamPtr, jlong playerPtr)
{
  oboePlayer *player_ptr = nullptr;

  if (playerPtr == 0 && streamPtr != 0)
  {
	decodeStream *decoder_ptr = reinterpret_cast<decodeStream *>(streamPtr);
	player_ptr = new oboePlayer();
	player_ptr->initStream(decoder_ptr, env);
  } else if (playerPtr != 0 && streamPtr != 0)
  {
	player_ptr = reinterpret_cast<oboePlayer *>(playerPtr);
	player_ptr->closePlay();
	player_ptr->openStream();
  }
  return reinterpret_cast<jlong>(player_ptr);
}

jboolean startPlay(JNIEnv *env, jobject activity, jlong playerPtr)
{
  if (playerPtr != 0)
  {
	oboePlayer *player_ptr = reinterpret_cast<oboePlayer *>(playerPtr);

	if (!player_ptr->startPlay())
	{
	  ALOGE("[%s] start oboe player error ", __FUNCTION__);
	  return false;
	}
	return true;
  }
  return false;
}

jbyteArray GetAudioAlbum(JNIEnv *env, jobject activity, jlong streamPtr, jstring path)
{

  decodeStream *stream_Ptr = reinterpret_cast<decodeStream *>(streamPtr);
  const char *absolutePath = env->GetStringUTFChars(path, nullptr);
  AudioAlbumCover cover = AudioAlbumCover(absolutePath, NAME_MAX);
  const std::pair<int, char *> pair = cover.getAlbumCover();

  if (pair.first > 0)
  {
	jbyteArray bufferArray = env->NewByteArray(pair.first);
	env->SetByteArrayRegion(bufferArray, 0, pair.first, (const jbyte *)(pair.second));
	delete[]pair.second;
	return bufferArray;
  }
  env->ReleaseStringUTFChars(path, absolutePath);
  jbyteArray array = env->NewByteArray(0);
  return array;
}

jint GetPlayStatus(JNIEnv *env, jobject activity, jlong playerPtr)
{
  if (playerPtr != 0)
  {
	oboePlayer *player_ptr = reinterpret_cast<oboePlayer *>(playerPtr);
	return player_ptr->getPlayerStatus();
  }
  return 1;
}

jboolean PausePlay(JNIEnv *env, jobject activity, jlong playerPtr)
{
  if (playerPtr != 0)
  {
	oboePlayer *player_ptr = reinterpret_cast<oboePlayer *>(playerPtr);
	return player_ptr->pausePlay();
  }
  return 1;
}

jboolean SeekPosition(JNIEnv *env, jobject activity, jlong position, jlong decodeStreamPtr)
{
  if (decodeStreamPtr == 0)
  {
	return false;
  }

  decodeStream *stream = reinterpret_cast<decodeStream *>(decodeStreamPtr);
  return stream->seekToPosition(position);
}

jlong GetAudioDuration(JNIEnv *env, jobject activity, jlong streamPtr)
{
  if (streamPtr == 0)
	return 0;

  decodeStream *stream = reinterpret_cast<decodeStream *>(streamPtr);
  int64_t duration = stream->getAudioDuration();
  return static_cast<jlong>(duration);
}

void SetPlayCircleType(JNIEnv *env, jobject nativeClass, jstring playType, jlong playerPtr)
{
  if (playerPtr == 0)
	return;
  oboePlayer *player = reinterpret_cast<oboePlayer *>(playerPtr);
  const char *type = env->GetStringUTFChars(playType, nullptr);
  player->setPlayCircleType(type);
  env->ReleaseStringUTFChars(playType, type);
}

jlongArray InitVideo(JNIEnv *env,
					 jobject nativeMethod,
					 jstring absolutePath,
					 jobject surface,
					 jlong streamPtr,
					 jlong playerPtr)
{
  const char *path = env->GetStringUTFChars(absolutePath, nullptr);
  jlongArray resultArray = env->NewLongArray(3);
  NativeWindowRender *render = new NativeWindowRender(nativeMethod, path, surface, env);
  render->init();
  decodeStream *audioStream = nullptr;
  oboePlayer *audioPlayer = nullptr;

  if (streamPtr == 0)
  {
	audioStream = new decodeStream(path, env, &nativeMethod);
  } else
  {
	audioStream = reinterpret_cast<decodeStream *>(streamPtr);
	audioStream->changeStream(path);
  }

  if (playerPtr == 0)
  {
	audioPlayer = new oboePlayer();
	audioPlayer->initStream(audioStream, env);
  } else
  {
	audioPlayer = reinterpret_cast<oboePlayer *>(playerPtr);
	audioPlayer->closePlay();
	audioPlayer->openStream();
  }
  jlong renderPtr = reinterpret_cast<jlong>(render);
  jlong audioStreamPtr = reinterpret_cast<jlong>(audioStream);
  jlong audioPlayerPtr = reinterpret_cast<jlong>(audioPlayer);
  env->SetLongArrayRegion(resultArray, 0, 1, &renderPtr);
  env->SetLongArrayRegion(resultArray, 1, 1, &audioStreamPtr);
  env->SetLongArrayRegion(resultArray, 2, 1, &audioPlayerPtr);
  env->ReleaseStringUTFChars(absolutePath, path);
  return resultArray;
}

void PlayVideo(JNIEnv *env,
			   jobject nativeMethod,
			   jlong windowPtr,
			   jlong audioStreamPtr,
			   jlong playerPtr)
{
  if (windowPtr == 0)
  {
	ALOGE("[%s] NativeWindow uninit", __FUNCTION__);
	return;
  }

  if (audioStreamPtr == 0)
  {
	ALOGE("[%s] audio stream uninit", __FUNCTION__);
	return;
  }

  if (playerPtr == 0)
  {
	ALOGE("[%s] oboe uninit", __FUNCTION__);
	return;
  }

  decodeStream *audioStream = reinterpret_cast<decodeStream *>(audioStreamPtr);
  audioStream->decodeFile();
  oboePlayer *player = reinterpret_cast<oboePlayer *>(playerPtr);
  player->startPlay();
  NativeWindowRender *render = reinterpret_cast<NativeWindowRender *>(windowPtr);
  render->play();
}

void SetVideoState(JNIEnv *env, jobject nativeMethod, jint state, jlong windowPtr)
{
  if (windowPtr == 0)
  {
	ALOGW("[%s] NativeWindow uninit", __FUNCTION__);
	return;
  }

  NativeWindowRender *render = reinterpret_cast<NativeWindowRender *>(windowPtr);
  render->setDecodeState(state);
}

void ScreenOrientationChange(JNIEnv *env, jobject nativeMethod, jobject surface, jlong windowPtr)
{
  if (windowPtr == 0)
  {
	ALOGW("[%s] NativeWindow uninit", __FUNCTION__);
	return;
  }

  NativeWindowRender *render = reinterpret_cast<NativeWindowRender *>(windowPtr);
  render->changeNativeWindow(surface, env);
}

jlong HwVideoStreamInit(JNIEnv *env, jobject nativeClass, jstring jlocationPath, jobject surface)
{
  const char *location = env->GetStringUTFChars(jlocationPath, nullptr);
  HwMediacodecPlayer *hwMediacodecPtr = new HwMediacodecPlayer(env, location, surface);
  env->ReleaseStringUTFChars(jlocationPath, location);
  return reinterpret_cast<jlong>(hwMediacodecPtr);
}

void HwVideoStartPlay(JNIEnv *env, jobject nativeClass, jlong hwMediacodecPlayerPtr)
{
  if (hwMediacodecPlayerPtr != 0)
  {
	HwMediacodecPlayer *mediacodecPlayer = reinterpret_cast<HwMediacodecPlayer *>(hwMediacodecPlayerPtr);
	mediacodecPlayer->openFFmpegcodec();
	mediacodecPlayer->initMediacodec();
	mediacodecPlayer->startMediacodec();
  }
}

void SetAudioSpeed(JNIEnv *env, jobject nativeClass, jfloat speed, jlong audioPtr)
{
  if (audioPtr == 0)
	return;

  oboePlayer *audioPlayer = reinterpret_cast<oboePlayer *>(audioPtr);
  audioPlayer->setSonicSpeed(speed);
}

long initRtspPushLiveStream(JNIEnv *env,
							jobject nativeClass,
							jstring rtspUrl,
							jint width,
							jint height)
{
  const char *str_rtspUrl = env->GetStringUTFChars(rtspUrl, nullptr);
  PushRtspLiveStream *rtspStream = new PushRtspLiveStream(str_rtspUrl, width, height);
  env->ReleaseStringUTFChars(rtspUrl, str_rtspUrl);
  return reinterpret_cast<long>(rtspStream);
}

void QueueInputBuffer(JNIEnv *env,
					  jobject nativeClass,
					  jobjectArray plane,
					  jint image_format,
					  jlong rtsp_stream_ptr)
{

}

void SetRtspExtraData(JNIEnv *env,
					  jobject nativeClass,
					  jobject byteBuffer,
					  jint arrayLength,
					  jlong rtspStreamPtr)
{
  uint8_t *data = static_cast<uint8_t *>(env->GetDirectBufferAddress(byteBuffer));

  if (data == nullptr)
  {
	ALOGE("[%s] get direct buffer error ", __FUNCTION__);
	return;
  }

  if (rtspStreamPtr == 0)
  {
	ALOGE("[%s] rtsp stream uninit", __FUNCTION__);
	return;
  }
  PushRtspLiveStream *rtspStream = reinterpret_cast<PushRtspLiveStream *>(rtspStreamPtr);
  rtspStream->setExtraData(data, arrayLength);
}

void PushRtspData(JNIEnv *env,
				  jobject nativeClass,
				  jobject byteArray,
				  jint bufferRemaining,
				  jlong ptsUs,
				  jboolean keyFrame,
				  jlong rtspStreamPtr)
{

  if (rtspStreamPtr == 0)
  {
	ALOGE("[%s] rtsp stream uninit", __FUNCTION__);
	return;
  }
  PushRtspLiveStream *rtspLiveStream = reinterpret_cast<PushRtspLiveStream *>(rtspStreamPtr);
  uint8_t *data = static_cast<uint8_t *>(env->GetDirectBufferAddress(byteArray));
  rtspLiveStream->writeIntervalFrame(data, bufferRemaining, ptsUs, keyFrame);
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
  JNIEnv *env = nullptr;
  vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);

  if (env != nullptr)
  {
	jclass musicNative = env->FindClass("com/example/xianyuplayer/MusicNativeMethod");
	JNINativeMethod musicNativeMethod[] = {
		{"getMetadata",             "(Ljava/lang/String;)[Lcom/example/xianyuplayer/database/MusicMetadata;",  (void *)getMetadata},
		{"startPlay",               "(J)Z",                                                                    (void *)startPlay},
		{"initPlay",                "(JJ)J",                                                                   (void *)initPlay},
		{"openDecodeStream",        "(Ljava/lang/String;JJ)J",                                                 (void *)openDecodeStream},
		{"startDecodeStream",       "(J)V",                                                                    (void *)startDecodeStream},
		{"getAudioAlbum",           "(JLjava/lang/String;)[B",                                                 (void *)GetAudioAlbum},
		{"getPlayStatus",           "(J)I",                                                                    (void *)GetPlayStatus},
		{"pausePlay",               "(J)Z",                                                                    (void *)PausePlay},
		{"seekPosition",            "(JJ)Z",                                                                   (void *)SeekPosition},
		{"getAudioDuration",        "(J)J",                                                                    (void *)GetAudioDuration},
		{"setPlayCircleType",       "(Ljava/lang/String;J)V",                                                  (void *)SetPlayCircleType},
		{"initVideo",               "(Ljava/lang/String;Landroid/view/Surface;JJ)[J",                          (void *)InitVideo},
		{"playVideo",               "(JJJ)V",                                                                  (void *)PlayVideo},
		{"setVideoState",           "(IJ)V",                                                                   (void *)SetVideoState},
		{"screenOrientationChange", "(Landroid/view/Surface;J)V",                                              (void *)ScreenOrientationChange},
		{"hwVideoStreamInit",       "(Ljava/lang/String;Landroid/view/Surface;)J",                             (void *)HwVideoStreamInit},
		{"hwVideoStartPlay",        "(J)V",                                                                    (void *)HwVideoStartPlay},
		{"setAudioSpeed",           "(FJ)V",                                                                   (void *)SetAudioSpeed},
		{"_initRtspPushLiveStream", "(Ljava/lang/String;II)J",                                                 (void *)initRtspPushLiveStream},
		{"queueInputBuffer",        "([Landroid/media/Image$Plane;IJ)V",                                       (void *)QueueInputBuffer},
		{"setRtspExtraData",        "(Ljava/nio/ByteBuffer;IJ)V",                                              (void *)SetRtspExtraData},
		{"pushRtspData",            "(Ljava/nio/ByteBuffer;IJZJ)V",                                            (void *)PushRtspData}
	};
	jint ret = env->RegisterNatives(musicNative,
									musicNativeMethod,
									sizeof(musicNativeMethod) / sizeof(musicNativeMethod[0]));

	if (ret != JNI_OK)
	{
	  ALOGE("[%s] register native method error ", __FUNCTION__);
	  return JNI_ERR;
	}
  }

  return JNI_VERSION_1_6;
}

}
VideoEncodeStream *videoEncode;
extern "C"
JNIEXPORT void JNICALL
Java_com_example_xianyuplayer_MusicNativeMethod_testEncode(JNIEnv *env, jobject thiz)
{
  videoEncode = new VideoEncodeStream();
  videoEncode->transformTest();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_xianyuplayer_MusicNativeMethod_testEncodeStop(JNIEnv *env, jobject thiz)
{
  delete videoEncode;
}
