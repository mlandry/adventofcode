# load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
# load("@bazel_tools//tools/build_defs/repo:maven_rules.bzl", "maven_jar")


# Java Guava stuff.
http_archive(
    name = "google_bazel_common",
    sha256 = "4d80b71a0f7d7828803dcce18016c07cf4040624776ae6a91fb4f377834ce46d",
    strip_prefix = "bazel-common-0646e08aa2a56f7db1c092a601c23c84f27712fb",
    urls = ["https://github.com/google/bazel-common/archive/0646e08aa2a56f7db1c092a601c23c84f27712fb.zip"],
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
