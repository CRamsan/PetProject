package com.cramsan.framework.logging.implementation

import com.cramsan.framework.logging.PlatformLoggerInterface
import com.cramsan.framework.logging.Severity

class EventLoggerInitializer(val targetSeverity: Severity, val platformLogger: PlatformLoggerInterface)