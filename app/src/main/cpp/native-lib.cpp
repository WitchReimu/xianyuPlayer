#include <jni.h>
#include <string>
#include "fileMetaDataInfo.h"
#include "decodeStream.h"
#define TAG "native-lib"

decodeStream *decoder_ptr = nullptr;

extern "C"
{
JNIEXPORT jstring JNICALL Java_com_example_xianyuplayer_MainActivity_stringFromJNI(
	JNIEnv *env,
	jobject /* this */)
{
	std::string hello = "Hello from C++";
	return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jobjectArray JNICALL
Java_com_example_xianyuplayer_MusicNativeMethod_getMetadata(JNIEnv *env, jobject thiz,
                                                            jstring file_path)
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

JNIEXPORT void JNICALL
Java_com_example_xianyuplayer_MusicNativeMethod_startPlay(JNIEnv *env, jobject thiz)
{

}

JNIEXPORT void JNICALL
Java_com_example_xianyuplayer_MusicNativeMethod_initStream(JNIEnv *env, jobject thiz, jstring path)
{
	const char *filePath = env->GetStringUTFChars(path, nullptr);
	if (decoder_ptr == nullptr)
		decoder_ptr = new decodeStream(filePath);
	env->ReleaseStringUTFChars(path, filePath);
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
//	JNIEnv *env = nullptr;
	return JNI_VERSION_1_6;
}
}

