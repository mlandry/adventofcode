load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:maven_rules.bzl", "maven_jar")


# Java Guava stuff.
http_archive(
    name = "google_bazel_common",
    sha256 = "005e5db64ef2f0562014fb4b7a86434bebac96c718d4eddcc1d89b563a5b2672",
    strip_prefix = "bazel-common-04b7bf73dd927d687e8184565e83cf0a58e69a34",
    urls = ["https://github.com/google/bazel-common/archive/04b7bf73dd927d687e8184565e83cf0a58e69a34.zip"],
)

load("@google_bazel_common//:workspace_defs.bzl", "google_common_workspace_rules")

google_common_workspace_rules()


# Kotlin stuff.
# rules_kotlin_version = "1.6.0"
# rules_kotlin_sha = "a57591404423a52bd6b18ebba7979e8cd2243534736c5c94d35c89718ea38f94"
# http_archive(
#    name = "io_bazel_rules_kotlin",
#    urls = ["https://github.com/bazelbuild/rules_kotlin/releases/download/v%s/rules_kotlin_release.tgz" % rules_kotlin_version],
#    sha256 = rules_kotlin_sha,
# )

# load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories")
# kotlin_repositories() # if you want the default. Otherwise see custom kotlinc distribution below

# load("@io_bazel_rules_kotlin//kotlin:core.bzl", "kt_register_toolchains")
# kt_register_toolchains() # to use the default toolchain, otherwise see toolchains below
