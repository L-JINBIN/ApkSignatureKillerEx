# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "-Wall -Wextra -Werror -fvisibility=hidden")

add_library( # Sets the name of the library.
        SignatureKiller

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/c/mt_jni.c
        src/main/c/xhook.c
        src/main/c/xh_core.c
        src/main/c/xh_elf.c
        src/main/c/xh_log.c
        src/main/c/xh_util.c
        src/main/c/xh_version.c)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries( # Specifies the target library.
        SignatureKiller

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
