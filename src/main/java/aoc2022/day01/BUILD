load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary")

java_binary(
    name = "calorie_counter",
    srcs = ["CalorieCounter.java"],
    resources = ["input.txt"],
    deps = [
      "//src/main/java/aoccommon:aoccommon",
    ],
)
