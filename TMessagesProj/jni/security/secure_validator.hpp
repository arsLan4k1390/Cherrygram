/*------------------------------------------------------------------------------------------------*/

#ifndef SECURE_VALIDATOR_HPP
#define SECURE_VALIDATOR_HPP

/*------------------------------------------------------------------------------------------------*/

#include <jni.h>

/*------------------------------------------------------------------------------------------------*/

namespace secure_validator {
    bool validate_signature(JNIEnv* env);
//    bool suspicious_classes_present(JNIEnv* env);
    bool has_xhook();
    bool has_jni_hook(JNIEnv* env);
    void maybeForceDisconnectOrUpdate(JNIEnv *env, int instanceNum, jint normalState);
}

/*------------------------------------------------------------------------------------------------*/

#endif //SECURE_VALIDATOR_HPP

/*------------------------------------------------------------------------------------------------*/