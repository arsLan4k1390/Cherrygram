/*------------------------------------------------------------------------------------------------*/

#ifndef JNIREFERENCE_HPP
#define JNIREFERENCE_HPP

/*------------------------------------------------------------------------------------------------*/

#include <jni.h>

/*------------------------------------------------------------------------------------------------*/

template<typename T> class JNILocalRef {
private:
    JNIEnv* env;
    T ref;
public:
    JNILocalRef(JNIEnv* env, T ref) : env(env), ref(ref) {

    }
    ~JNILocalRef() {
        if (!env || !ref)
            return;
        env->DeleteLocalRef(ref);
    }
public:
    T get() const {
        return ref;
    }
    operator T() const {
        return ref;
    }
};

/*------------------------------------------------------------------------------------------------*/

#endif //JNIREFERENCE_HPP

/*------------------------------------------------------------------------------------------------*/
