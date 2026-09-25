# Application-specific R8 rules.
#
# Nothing extra is needed to keep the app working: it uses no reflection or serialization,
# and AndroidX (Compose, Navigation, Lifecycle, DataStore) ships its own consumer rules.
# Blanket -keep rules here would stop R8 from shrinking the app, so don't add them back.

# Readable stack traces in crash reports (map with build/outputs/mapping/release/mapping.txt)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
