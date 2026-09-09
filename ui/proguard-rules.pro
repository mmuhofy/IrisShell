# Iris Shell - module ProGuard rules for :ui.
# Only rules that affect THIS module's release build belong here.
#
# Cross-module consumer rules belong in consumer-rules.pro for this module.

# Hilt ViewModel HiltModules are generated at compile time and referenced
# through reflection. Keep all ui module classes in release.
-keep class com.iris.irisshell.ui.** { *; }
-dontwarn com.iris.irisshell.ui.**
