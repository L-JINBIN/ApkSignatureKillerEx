#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <malloc.h>
#include <unistd.h>
#include <dirent.h>
#include <stdbool.h>
#include <string.h>
#include "xhook.h"
#include "xh_log.h"

const char *apkPath__;
const char *repPath__;

int (*old_open)(const char *, int, mode_t);
static int openImpl(const char *pathname, int flags, mode_t mode) {
    //XH_LOG_ERROR("open: %s", pathname);
    if (strcmp(pathname, apkPath__) == 0){
        //XH_LOG_ERROR("replace -> %s", repPath__);
        return old_open(repPath__, flags, mode);
    }
    return old_open(pathname, flags, mode);
}

int (*old_open64)(const char *, int, mode_t);
static int open64Impl(const char *pathname, int flags, mode_t mode) {
    //XH_LOG_ERROR("open64: %s", pathname);
    if (strcmp(pathname, apkPath__) == 0){
        //XH_LOG_ERROR("replace -> %s", repPath__);
        return old_open64(repPath__, flags, mode);
    }
    return old_open64(pathname, flags, mode);
}

int (*old_openat)(int, const char*, int, mode_t);
static int openatImpl(int fd, const char *pathname, int flags, mode_t mode) {
    //XH_LOG_ERROR("openat: %s", pathname);
    if (strcmp(pathname, apkPath__) == 0){
        //XH_LOG_ERROR("replace -> %s", repPath__);
        return old_openat(fd, repPath__, flags, mode);
    }
    return old_openat(fd, pathname, flags, mode);
}

int (*old_openat64)(int, const char*, int, mode_t);
static int openat64Impl(int fd, const char *pathname, int flags, mode_t mode) {
    //XH_LOG_ERROR("openat64: %s", pathname);
    if (strcmp(pathname, apkPath__) == 0){
        //XH_LOG_ERROR("replace -> %s", repPath__);
        return old_openat64(fd, repPath__, flags, mode);
    }
    return old_openat64(fd, pathname, flags, mode);
}

JNIEXPORT void JNICALL
Java_bin_mt_signature_KillerApplication_hookApkPath(JNIEnv *env, __attribute__((unused)) jclass clazz, jstring apkPath, jstring repPath) {
    apkPath__ = (*env)->GetStringUTFChars(env, apkPath, 0);
    repPath__ = (*env)->GetStringUTFChars(env, repPath, 0);

    xhook_register(".*\\.so$", "openat64", openat64Impl, (void **) &old_openat64);
    xhook_register(".*\\.so$", "openat", openatImpl, (void **) &old_openat);
    xhook_register(".*\\.so$", "open64", open64Impl, (void **) &old_open64);
    xhook_register(".*\\.so$", "open", openImpl, (void **) &old_open);

    xhook_refresh(0);
}
