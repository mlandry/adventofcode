load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "google_bazel_common",
    sha256 = "a0d023e73fa01c93269445b13f2bdfc381e72e9527e06ae5411ab431ceb18d56",
    strip_prefix = "bazel-common-b89cd874d40250d9bab356d40f6ffac8f7aa98f1",
    urls = ["https://github.com/google/bazel-common/archive/b89cd874d40250d9bab356d40f6ffac8f7aa98f1.zip"],
)

load("@google_bazel_common//:workspace_defs.bzl", "google_common_workspace_rules")

google_common_workspace_rules()