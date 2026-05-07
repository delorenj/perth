# syntax=docker/dockerfile:1.7
# Multi-stage build: compile the bridge in a Rust toolchain image, ship the
# release binary on a minimal Debian image with the zellij CLI available.

FROM rust:1.85-bookworm AS builder
WORKDIR /build
COPY Cargo.toml Cargo.lock* ./
COPY src ./src
# Build with --locked when a lockfile is present; otherwise fall back to
# regular release build. The first run on a fresh checkout has no lockfile.
RUN if [ -f Cargo.lock ]; then cargo build --release --locked; else cargo build --release; fi

FROM debian:bookworm-slim AS runtime
ARG ZELLIJ_VERSION=0.44.0

# zellij ships static x86_64 / aarch64 binaries; pick by buildplatform.
RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates curl xz-utils \
 && rm -rf /var/lib/apt/lists/* \
 && case "$(uname -m)" in \
      x86_64)  ARCH=x86_64-unknown-linux-musl ;; \
      aarch64) ARCH=aarch64-unknown-linux-musl ;; \
      *) echo "unsupported arch $(uname -m)" && exit 1 ;; \
    esac \
 && curl -fsSL "https://github.com/zellij-org/zellij/releases/download/v${ZELLIJ_VERSION}/zellij-${ARCH}.tar.gz" \
    | tar -xz -C /usr/local/bin

COPY --from=builder /build/target/release/perth-bridge /usr/local/bin/perth-bridge

# Default to listening on all interfaces so the container is reachable from
# Traefik. Override at runtime with --host/--port.
EXPOSE 7800
ENTRYPOINT ["/usr/local/bin/perth-bridge"]
CMD ["--host", "0.0.0.0", "--port", "7800"]
