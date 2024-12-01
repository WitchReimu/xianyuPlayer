//
// Created by Administrator on 2024/11/22.
//

#ifndef LOGUTILS_H
#define LOGUTILS_H
#include <android/log.h>
#define ALOGE(args...) __android_log_print(ANDROID_LOG_ERROR, TAG, args)
#define ALOGI(args...) __android_log_print(ANDROID_LOG_INFO, TAG, args)
#define ALOGW(args...) __android_log_print(ANDROID_LOG_WARN, TAG, args)
#endif //LOGUTILS_H
