#include <jni.h>
#include <string>
#include "fileMetaDataInfo.h"
#include "decodeStream.h"
#include "oboePlayer.h"
#define TAG "native-lib"

decodeStream *decoder_ptr = nullptr;

oboePlayer *player_ptr = nullptr;

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

void initStream(JNIEnv *env, jobject activity, jstring path)
{
	const char *filePath = env->GetStringUTFChars(path, nullptr);
	if (decoder_ptr == nullptr)
		decoder_ptr = new decodeStream(filePath);
	env->ReleaseStringUTFChars(path, filePath);
}

void startDecodeStream(JNIEnv *env, jobject activity)
{
	if (decoder_ptr != nullptr)
	{
		decoder_ptr->decodeFile();
	}
}

void initPlay(JNIEnv *env, jobject activity)
{
	if (player_ptr == nullptr && decoder_ptr != nullptr)
	{
		player_ptr = new oboePlayer();
		player_ptr->initStream(decoder_ptr);
	}
}

void startPlay(JNIEnv *env, jobject activity)
{
	if (player_ptr != nullptr)
	{
		if (!player_ptr->startPlay())
		{
			ALOGE("[%s] start oboe player error ", __FUNCTION__);
		}
	}
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
		JNINativeMethod musicNativeMethod[] = {{"getMetadata",
		                                        "(Ljava/lang/String;)[Lcom/example/xianyuplayer/database/MusicMetadata;",
		                                        (void *)getMetadata},
		                                       {"startPlay", "()V", (void *)startPlay},
		                                       {"initPlay", "()V", (void *)initPlay},
		                                       {"initStream", "(Ljava/lang/String;)V",
		                                        (void *)initStream},
		                                       {"startDecodeStream", "()V",
		                                        (void *)startDecodeStream}

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
