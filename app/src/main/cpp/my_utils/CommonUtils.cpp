//
// Created by toptech-6 on 2025/1/21.
//
#include "CommonUtils.h"

/**
 *
 * @param jvm 传入有效的jvm
 * @param isAttach 传入一个布尔变量，将env是否绑定的状态赋值给该变量。
 * @return  返回一个绑定了当前线程的JNIEnv对象，如果获取失败返回空指针
 */
JNIEnv *getJniEnv(JavaVM *jvm, bool &isAttach)
{
  const char *TAG = "CommonUtils getJniEnv";
  JNIEnv *env = nullptr;
  isAttach = false;

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
