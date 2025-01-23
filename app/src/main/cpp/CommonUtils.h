//
// Created by Administrator on 2024/11/22.
//

#ifndef LOGUTILS_H
#define LOGUTILS_H
#include <android/log.h>
#include <jni.h>

#define ALOGE(args...) __android_log_print(ANDROID_LOG_ERROR, TAG, args)
#define ALOGI(args...) __android_log_print(ANDROID_LOG_INFO, TAG, args)
#define ALOGW(args...) __android_log_print(ANDROID_LOG_WARN, TAG, args)

enum playListCircle_enum : int
{
  singleCircle = 0,
  listCircle
};

JNIEnv *getJniEnv(JavaVM *jvm, bool &isAttach);
#endif //LOGUTILS_H
