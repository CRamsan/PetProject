package com.cramsan.framework.thread.implementation

import com.cramsan.framework.thread.ThreadUtilInterface
import com.cramsan.framework.thread.ThreadUtilPlatformInitializerInteface

class ThreadUtilAndroidInitializer(override val platformThreadUtil: ThreadUtilInterface) : ThreadUtilPlatformInitializerInteface
