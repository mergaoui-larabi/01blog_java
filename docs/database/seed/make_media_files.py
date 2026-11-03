#!/usr/bin/env python3
"""Write placeholder image files for the seeded medias rows.

Reads storage_key, width and height from the blog_medias database through the
running container, writes a PNG at each path under the media service storage
directory, then updates size_bytes to the real file size so the table describes
what is on disk. Run it after 02_medias.sql.

Only images are produced: 02_medias.sql seeds no POST_VIDEO rows because there
is no video encoder on this machine to generate a playable file for them.
"""

import struct
import subprocess
import sys
import zlib
from pathlib import Path

CONTAINER = "blog-postgres-media"
STORAGE_ROOT = Path(__file__).resolve().parents[3] / "backend/media-service/media-storage"
STRIPE_PERIOD = 16


def psql(sql: str) -> str:
    """Run SQL inside the media container and return raw output."""
    result = subprocess.run(
        ["docker", "exec", "-i", CONTAINER, "sh", "-c",
         'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 -At -f -'],
        input=sql.encode(),
        stdout=subprocess.PIPE,
        check=True,
    )
    return result.stdout.decode()


def png_bytes(width: int, height: int, base: bytes, accent: bytes) -> bytes:
    """A two-colour striped PNG, built without any image library."""
    row_base = base * width
    row_accent = accent * width
    rows = []
    for y in range(height):
        row = row_base if (y // STRIPE_PERIOD) % 2 == 0 else row_accent
        rows.append(b"\x00" + row)

    def chunk(tag: bytes, data: bytes) -> bytes:
        return (struct.pack(">I", len(data)) + tag + data
                + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF))

    return (b"\x89PNG\r\n\x1a\n"
            + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(b"".join(rows), 6))
            + chunk(b"IEND", b""))


def colours(key: str) -> tuple[bytes, bytes]:
    seed = zlib.crc32(key.encode())
    base = bytes([(seed >> shift) & 0xFF | 0x40 for shift in (0, 8, 16)])
    accent = bytes([(255 - c) & 0xFF for c in base])
    return base, accent


def main() -> int:
    rows = psql("select storage_key, width, height from medias order by storage_key;")
    if not rows.strip():
        print("No medias rows found. Apply docs/database/seed/02_medias.sql first.", file=sys.stderr)
        return 1

    sizes: dict[str, int] = {}
    for line in rows.splitlines():
        storage_key, width, height = line.split("|")
        path = STORAGE_ROOT / storage_key
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(png_bytes(int(width), int(height), *colours(storage_key)))
        sizes[storage_key] = path.stat().st_size

    updates = "\n".join(
        f"update medias set size_bytes = {size}, updated_at = now() "
        f"where storage_key = '{key}';"
        for key, size in sizes.items()
    )
    psql(updates)
    print(f"Wrote {len(sizes)} files under {STORAGE_ROOT} and synced size_bytes.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
