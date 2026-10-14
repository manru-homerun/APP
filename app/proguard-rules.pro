# Kakao login models are read through reflection by the SDK.
-keep class com.kakao.sdk.**.model.* { <fields>; }

# Kakao Map uses native code that requires these Java entry points.
-keep class com.kakao.vectormap.** { *; }
-keep interface com.kakao.vectormap.**
