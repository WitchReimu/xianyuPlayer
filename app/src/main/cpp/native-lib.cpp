#include <jni.h>
#include <string>
#include "fileMetaDataInfo.h"
#include "decodeStream.h"
#include "oboePlayer.h"
#include "AudioAlbumCover.h"
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

jlong
openDecodeStream(JNIEnv *env, jobject natvieClass, jstring path, jlong streamPtr, jlong playerPtr)
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

jlong
initPlay(JNIEnv *env, jobject activity, jlong streamPtr, jlong playerPtr)
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

JNIEXPORT jstring JNICALL Java_com_example_xianyuplayer_MainActivity_stringFromJNI(
	JNIEnv *env,
	jobject /* this */)
{
  std::string hello = "Hello from C++";
  return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
  JNIEnv *env = nullptr;
  vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);

  if (env != nullptr)
  {
	jclass musicNative = env->FindClass("com/example/xianyuplayer/MusicNativeMethod");
	JNINativeMethod musicNativeMethod[] = {
		{"getMetadata",       "(Ljava/lang/String;)[Lcom/example/xianyuplayer/database/MusicMetadata;", (void *)getMetadata},
		{"startPlay",         "(J)Z",                                                                   (void *)startPlay},
		{"initPlay",          "(JJ)J",                                                                  (void *)initPlay},
		{"openDecodeStream",  "(Ljava/lang/String;JJ)J",                                                (void *)openDecodeStream},
		{"startDecodeStream", "(J)V",                                                                   (void *)startDecodeStream},
		{"getAudioAlbum",     "(JLjava/lang/String;)[B",                                                (void *)GetAudioAlbum},
		{"getPlayStatus",     "(J)I",                                                                   (void *)GetPlayStatus},
		{"pausePlay",         "(J)Z",                                                                   (void *)PausePlay},
		{"seekPosition",      "(JJ)Z",                                                                  (void *)SeekPosition},
		{"getAudioDuration",  "(J)J",                                                                   (void *)GetAudioDuration}
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
