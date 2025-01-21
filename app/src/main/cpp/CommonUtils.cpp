//
// Created by toptech-6 on 2025/1/21.
//
#include "CommonUtils.h"

JNIEnv *getJniEnv(JavaVM *jvm, bool &isAttach)
{
  const char *TAG = "CommonUtils getJniEnv";
  JNIEnv *env = nullptr;

  if (jvm == nullptr)
  {
	return env;
  }

  jint ret = jvm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);
  if (ret != JNI_OK)
  {

	if (ret == JNI_EDETACHED)
	{
	  ret = jvm->AttachCurrentThread(&env, nullptr);
	  if (ret == JNI_OK)
	  {
		ALOGI("[%s] JNIenv attach thread success", __FUNCTION__);
		isAttach = true;
		return env;
	  }
	}

	ALOGW("[%s] get JNIEnv is NULL", __FUNCTION__);
	return nullptr;
  }
  isAttach = false;
  return env;
}
