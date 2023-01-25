//
// Created by anton on 25/01/2023.
//

#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_yoga_mborasystem_util_Ultilitario_baseUrlFromJNI(JNIEnv *env, jclass clazz) {
    std::string baseUrl = "https://yogatcs.site/mborasystem-admin/public/api/";
    return env->NewStringUTF(baseUrl.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_yoga_mborasystem_util_Ultilitario_tokenFCMFromJNI(JNIEnv *env, jclass clazz) {
    std::string baseUrl = "AAAA7nu_waA:APA91bGbM9mbMQeu-BN0ArnKW_cvDG1J_pCQWydeUgbDile3lxg93b8I0cQxijn0dO7O9FbHO5Iwmnlr_M5WoEqWEGYbubXA4u_kh9xeIO86oVXD2vmOWwEfiIKZRJkRIb6MX195QTBY";
    return env->NewStringUTF(baseUrl.c_str());
}
