# Iris Shell - module ProGuard rules for :design-system.
# Only rules that affect THIS module's release build belong here.
#
# Cross-module consumer rules belong in consumer-rules.pro for this module.

# IrisColors.kt defines top-level Color val properties accessed via static getters
# (IrisColorsKt). R8 full mode strips these as "unused" when they are only
# referenced through Compose @Composable code.
-keep class com.iris.irisshell.design.system.** { *; }
-keepclassmembers class com.iris.irisshell.design.system.IrisColorsKt {
    public static **<methods>;
}
-dontwarn com.iris.irisshell.design.system.**
