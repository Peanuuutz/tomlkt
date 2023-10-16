package test

import org.intellij.lang.annotations.Language

fun main() {

}

@Language("toml")
const val SmallSampleConfig: String = """
# This is a TOML document.

description = '''
TOML Example
This is a multiline literal string
\ \ \ <- Literally 3 '\'
'''

[owner]
name = "Tom Preston-Werner"
# dob = 1979-05-27T07:32:00-08:00 # First class dates.

[database]
server = "192.168.1.1"
ports = [ 8000, 8001, 8002 ]
connection_max = 5000
enabled = true

[servers]

  # Indentation (tabs and/or spaces) is allowed but not required.
  [servers.alpha]
  ip = "10.0.0.1"
  dc = "eqdc10"

  [servers.beta]
  ip = "10.0.0.2"
  dc = "eqdc10"

[clients]
data = [ ["gamma", "delta"], [1, 2] ]

# Line breaks are OK when inside arrays.
hosts = [
  "alpha",
  "omega"
]
"""

@Language("toml")
const val LargeSampleConfig: String = """
[workspace]
resolver = "2"
members = [
  "crates/*",
  "credential/*",
  "benches/benchsuite",
  "benches/capture",
]
exclude = [
  "target/", # exclude bench testing
]

[workspace.package]
rust-version = "1.73"  # MSRV:1
edition = "2021"
license = "MIT OR Apache-2.0"

[workspace.dependencies]
anstream = "0.6.4"
anstyle = "1.0.4"
anyhow = "1.0.75"
base64 = "0.21.4"
bytesize = "1.3"
cargo = { path = "" }
cargo-credential = { version = "0.4.0", path = "credential/cargo-credential" }
cargo-credential-libsecret = { version = "0.3.1", path = "credential/cargo-credential-libsecret" }
cargo-credential-macos-keychain = { version = "0.3.0", path = "credential/cargo-credential-macos-keychain" }
cargo-credential-wincred = { version = "0.3.0", path = "credential/cargo-credential-wincred" }
cargo-platform = { path = "crates/cargo-platform", version = "0.1.4" }
cargo-test-macro = { path = "crates/cargo-test-macro" }
cargo-test-support = { path = "crates/cargo-test-support" }
cargo-util = { version = "0.2.6", path = "crates/cargo-util" }
cargo_metadata = "0.18.0"
clap = "4.4.6"
color-print = "0.3.5"
core-foundation = { version = "0.9.3", features = ["mac_os_10_7_support"] }
crates-io = { version = "0.39.0", path = "crates/crates-io" }
criterion = { version = "0.5.1", features = ["html_reports"] }
curl = "0.4.44"
curl-sys = "0.4.68"
filetime = "0.2.22"
flate2 = { version = "1.0.27", default-features = false, features = ["zlib"] }
git2 = "0.18.1"
git2-curl = "0.19.0"
gix = { version = "0.54.1", default-features = false, features = ["blocking-http-transport-curl"] }
gix-features-for-configuration-only = { version = "0.35.0", package = "gix-features", features = [ "parallel" ] }
glob = "0.3.1"
handlebars = { version = "3.5.5", features = ["dir_source"] }
hex = "0.4.3"
hmac = "0.12.1"
home = "0.5.5"
http-auth = { version = "0.1.8", default-features = false }
humantime = "2.1.0"
ignore = "0.4.20"
im-rc = "15.1.0"
indexmap = "2"
itertools = "0.11.0"
jobserver = "0.1.26"
lazycell = "1.3.0"
libc = "0.2.148"
libgit2-sys = "0.16.1"
libloading = "0.8.1"
memchr = "2.6.4"
miow = "0.6.0"
opener = "0.6.1"
openssl ="0.10.57"
os_info = "3.7.0"
pasetors = { version = "0.6.7", features = ["v3"] }
pathdiff = "0.2"
percent-encoding = "2.3"
pkg-config = "0.3.27"
pretty_assertions = "1.4.0"
proptest = "1.3.1"
pulldown-cmark = { version = "0.9.3", default-features = false }
rand = "0.8.5"
rustfix = "0.6.1"
same-file = "1.0.6"
security-framework = "2.9.2"
semver = { version = "1.0.20", features = ["serde"] }
serde = "1.0.188"
serde-untagged = "0.1.1"
serde-value = "0.7.0"
serde_ignored = "0.1.9"
serde_json = "1.0.107"
sha1 = "0.10.6"
sha2 = "0.10.8"
shell-escape = "0.1.5"
snapbox = { version = "0.4.13", features = ["diff"] }
syn = { version = "2.0.37", features = ["extra-traits"] }
tar = { version = "0.4.40", default-features = false }
tempfile = "3.8.0"
thiserror = "1.0.49"
time = { version = "0.3", features = ["parsing"] }
toml = "0.8.2"
toml_edit = { version = "0.20.2", features = ["serde"] }
tracing = "0.1.37"
tracing-subscriber = { version = "0.3.17", features = ["env-filter"] }
unicase = "2.7.0"
unicode-width = "0.1.11"
unicode-xid = "0.2.4"
url = "2.4.1"
varisat = "0.2.2"
walkdir = "2.4.0"
windows-sys = "0.48"

[package]
name = "cargo"
version = "0.76.0"
edition = true
license = true
rust-version = true
homepage = "https://crates.io"
repository = "https://github.com/rust-lang/cargo"
documentation = "https://docs.rs/cargo"
description = '''
Cargo, a package manager for Rust.
'''

[lib]
name = "cargo"
path = "src/cargo/lib.rs"

[dependencies]
anstream = true
anstyle = true
anyhow = true
base64 = true
bytesize = true
cargo-credential = true
cargo-credential-libsecret = true
cargo-credential-macos-keychain = true
cargo-credential-wincred = true
cargo-platform = true
cargo-util = true
clap = { workspace = true, features = ["wrap_help"] }
color-print = true
crates-io = true
curl = { workspace = true, features = ["http2"] }
curl-sys = true
filetime = true
flate2 = true
git2 = true
git2-curl = true
gix = true
gix-features-for-configuration-only = true
glob = true
hex = true
hmac = true
home = true
http-auth = true
humantime = true
ignore = true
im-rc = true
indexmap = true
itertools = true
jobserver = true
lazycell = true
libc = true
libgit2-sys = true
memchr = true
opener = true
os_info = true
pasetors = true
pathdiff = true
pulldown-cmark = true
rand = true
rustfix = true
semver = true
serde = { workspace = true, features = ["derive"] }
serde-untagged = true
serde-value = true
serde_ignored = true
serde_json = { workspace = true, features = ["raw_value"] }
sha1 = true
shell-escape = true
syn = true
tar = true
tempfile = true
time = true
toml = true
toml_edit = true
tracing = true
tracing-subscriber = true
unicase = true
unicode-width = true
unicode-xid = true
url = true
walkdir = true

[target.not-window.dependencies]
openssl = { workspace = true, optional = true }

[target.window.dependencies.windows-sys]
workspace = true
features = [
  "Win32_Foundation",
  "Win32_Security",
  "Win32_Storage_FileSystem",
  "Win32_System_IO",
  "Win32_System_Console",
  "Win32_System_JobObjects",
  "Win32_System_Threading",
]

[dev-dependencies]
cargo-test-macro = true
cargo-test-support = true
same-file = true
snapbox = true

[build-dependencies]
flate2 = true
tar = true

[[bin]]
name = "cargo"
test = false
doc = false

[features]
vendored-openssl = ["openssl/vendored"]
vendored-libgit2 = ["libgit2-sys/vendored"]
# This is primarily used by rust-lang/rust distributing cargo the executable.
all-static = ['vendored-openssl', 'curl/static-curl', 'curl/force-system-lib-on-osx']
"""
