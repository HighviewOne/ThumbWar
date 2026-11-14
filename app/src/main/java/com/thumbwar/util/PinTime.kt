package com.thumbwar.util

import java.util.Locale

/** A pin time for display, e.g. 3240 ms → "3.2 s" (with the phone's decimal separator). */
fun formatPinTime(ms: Long): String = String.format(Locale.getDefault(), "%.1f s", ms / 1000.0)
