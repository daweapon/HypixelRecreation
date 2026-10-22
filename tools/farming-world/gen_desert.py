"""Appends a generated placeholder Mushroom Desert to the Hub polar world, keeping every real Hub chunk byte-for-byte.

usage: python3 gen_desert.py <hub.polar> <out.polar>   (needs Python 3.14 for compression.zstd)
Replace the output file with a real Farming Islands map when one exists; every wiki NPC coordinate is honoured.
"""
import math
import struct
import sys
from compression import zstd

MIN_SECTION, MAX_SECTION = -4, 19
SEA = 75
X_RANGE = (48, 480)
Z_RANGE = (-672, -272)


def var(value):
    out = bytearray()
    value &= 0xFFFFFFFF
    while True:
        part = value & 0x7F
        value >>= 7
        if value:
            out.append(part | 0x80)
        else:
            out.append(part)
            return bytes(out)


def read_var(data, pos):
    value = shift = 0
    while True:
        byte = data[pos]
        pos += 1
        value |= (byte & 0x7F) << shift
        shift += 7
        if not byte & 0x80:
            return value, pos


def string(text):
    raw = text.encode()
    return var(len(raw)) + raw


def split_polar(path):
    data = open(path, "rb").read()
    pos = 6
    data_version, pos = read_var(data, pos)
    compression = data[pos]
    pos += 1
    _, pos = read_var(data, pos)
    body = zstd.decompress(data[pos:]) if compression == 1 else data[pos:]
    return data[:6], data_version, body


def parse_chunk_coords(body):
    pos = 2
    user_len, pos = read_var(body, pos)
    pos += user_len
    count_pos = pos
    count, pos = read_var(body, pos)
    return count_pos, count, pos


def existing_chunks(body):
    import pickle  # noqa: F401
    sys.path.insert(0, "/home/bradl/hypixel-dev/.claude/crimson-work/polartool")
    import polar
    import tempfile
    import os
    handle, tmp = tempfile.mkstemp()
    os.write(handle, body)
    os.close(handle)
    try:
        _, _, chunks = polar.load(tmp)
    finally:
        os.remove(tmp)
    return set(chunks.keys())


def smooth(t):
    t = max(0.0, min(1.0, t))
    return t * t * (3 - 2 * t)


def hash2(x, z):
    n = (x * 374761393 + z * 668265263) & 0xFFFFFFFF
    n = ((n ^ (n >> 13)) * 1274126177) & 0xFFFFFFFF
    return ((n ^ (n >> 16)) & 0xFFFF) / 65535.0


def noise(x, z, scale):
    gx, gz = x / scale, z / scale
    x0, z0 = math.floor(gx), math.floor(gz)
    fx, fz = smooth(gx - x0), smooth(gz - z0)
    a = hash2(x0, z0) * (1 - fx) + hash2(x0 + 1, z0) * fx
    b = hash2(x0, z0 + 1) * (1 - fx) + hash2(x0 + 1, z0 + 1) * fx
    return a * (1 - fz) + b * fz


class Platform:
    def __init__(self, x, z, inner, outer, y):
        self.x, self.z, self.inner, self.outer, self.y = x, z, inner, outer, y

    def weight(self, x, z):
        d = math.hypot(x - self.x, z - self.z)
        return 1 - smooth((d - self.inner) / (self.outer - self.inner))


class Capsule(Platform):
    def __init__(self, a, b, inner, outer, ya, yb):
        super().__init__(a[0], a[1], inner, outer, ya)
        self.a, self.b, self.ya, self.yb = a, b, ya, yb

    def project(self, x, z):
        ax, az = self.a
        bx, bz = self.b
        t = ((x - ax) * (bx - ax) + (z - az) * (bz - az)) / ((bx - ax) ** 2 + (bz - az) ** 2)
        return max(0.0, min(1.0, t))

    def weight(self, x, z):
        t = self.project(x, z)
        px = self.a[0] + (self.b[0] - self.a[0]) * t
        pz = self.a[1] + (self.b[1] - self.a[1]) * t
        return 1 - smooth((math.hypot(x - px, z - pz) - self.inner) / (self.outer - self.inner))

    def level(self, x, z):
        return self.ya + (self.yb - self.ya) * self.project(x, z)


MOUNTAIN = (261.5, -565.5)
MOUNTAIN_TOP = 183
MOUNTAIN_SLOPE = 1.6
MOUNTAIN_FLAT = 6
GORGE = (275.0, -495.0)
OASIS = (322.0, -435.0, 18.0, 13.0)
CAVE = (192.0, -486.0, 34.0, 30.0)
CAVE_FLOOR = 42
CAVE_HEIGHT = 12

PLATFORMS = [
    Platform(170, -362, 25, 36, 76),
    Platform(200.5, -437, 8, 28, 91),
    Platform(319.5, -470.5, 10, 32, 101),
    Platform(283, -545, 14, 30, 103),
    Platform(297.5, -562.5, 5, 18, 86),
    Platform(388.5, -372.5, 14, 30, 84),
    Platform(261.5, -565.5, 6, 9, 183),
]
CAUSEWAY = Capsule((319.5, -470.5), (283, -545), 6, 12, 101, 103)


def natural(x, z):
    return 76 + (noise(x, z, 24) - 0.5) * 6 + (noise(x, z, 7) - 0.5) * 2


def height(x, z):
    h = natural(x, z)
    d = math.hypot(x - MOUNTAIN[0], z - MOUNTAIN[1])
    cone = MOUNTAIN_TOP - MOUNTAIN_SLOPE * max(0.0, d - MOUNTAIN_FLAT)
    h = max(h, cone)
    gd = math.hypot((x - GORGE[0]) / 1.0, (z - GORGE[1]) / 1.45)
    wg = 1 - smooth((gd - 9) / 31)
    h = h * (1 - wg) + 47 * wg
    ox, oz, rx, rz = OASIS
    od = math.hypot((x - ox) / rx, (z - oz) / rz)
    wo = 1 - smooth((od - 0.6) / 0.55)
    h = h * (1 - wo) + 70 * wo
    for platform in PLATFORMS:
        w = platform.weight(x, z)
        if w > 0:
            h = h * (1 - w) + platform.y * w
    w = CAUSEWAY.weight(x, z)
    if w > 0:
        h = h * (1 - w) + CAUSEWAY.level(x, z) * w
    return int(round(h))


def ramp_cells():
    cells = {}
    r_start, r_end, turns = 73.0, MOUNTAIN_FLAT + 3, 3.0
    steps = 4000
    for i in range(steps + 1):
        t = i / steps
        r = r_start + (r_end - r_start) * t
        theta = math.pi * 0.5 + turns * 2 * math.pi * t
        y = MOUNTAIN_TOP - MOUNTAIN_SLOPE * (r - MOUNTAIN_FLAT)
        cx = MOUNTAIN[0] + r * math.cos(theta)
        cz = MOUNTAIN[1] + r * math.sin(theta)
        tx, tz = -math.sin(theta), math.cos(theta)
        for off in (-1.5, -1.0, -0.5, 0, 0.5, 1.0, 1.5):
            px, pz = int(math.floor(cx + tx * off)), int(math.floor(cz + tz * off))
            level = int(round(y))
            if (px, pz) not in cells or cells[(px, pz)] < level:
                cells[(px, pz)] = level
    return cells


class World:
    def __init__(self):
        self.blocks = {}

    def set(self, x, y, z, name):
        self.blocks[(x, y, z)] = name

    def fill(self, x1, y1, z1, x2, y2, z2, name):
        for x in range(min(x1, x2), max(x1, x2) + 1):
            for y in range(min(y1, y2), max(y1, y2) + 1):
                for z in range(min(z1, z2), max(z1, z2) + 1):
                    self.set(x, y, z, name)


def surface_block(y, depth_below):
    if y >= 140:
        return "minecraft:stone"
    if y >= 110:
        return "minecraft:red_sandstone" if depth_below > 0 else "minecraft:terracotta"
    if depth_below == 0:
        return "minecraft:sand"
    if depth_below < 4:
        return "minecraft:sand" if y > 60 else "minecraft:sandstone"
    return "minecraft:sandstone" if depth_below < 9 else "minecraft:stone"


STAIRS_NORTH = "minecraft:stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]"
BRICK = "minecraft:stone_bricks"
AIR = "minecraft:air"


def room(world, x1, x2, z1, z2, feet, height=5):
    world.fill(x1 - 1, feet - 1, z1 - 1, x2 + 1, feet + height, z2 + 1, BRICK)
    world.fill(x1, feet, z1, x2, feet + height - 1, z2, AIR)


def tunnel_x(world, x, z1, z2, feet, height=3):
    world.fill(x, feet - 1, z1 - 1, x, feet + height, z2 + 1, BRICK)
    world.fill(x, feet, z1, x, feet + height - 1, z2, AIR)


def carve_cave(world):
    cx, cz, rx, rz = CAVE
    for x in range(int(cx - rx) - 1, int(cx + rx) + 2):
        for z in range(int(cz - rz) - 1, int(cz + rz) + 2):
            d2 = ((x - cx) / rx) ** 2 + ((z - cz) / rz) ** 2
            if d2 >= 1:
                continue
            ceiling = CAVE_FLOOR + 1 + int(CAVE_HEIGHT * math.sqrt(1 - d2))
            world.fill(x, CAVE_FLOOR + 1, z, x, ceiling, z, AIR)
            world.set(x, CAVE_FLOOR, z, "minecraft:mycelium" if hash2(x, z) < 0.3 else "minecraft:stone")
            if hash2(x + 7, z - 3) < 0.012:
                world.set(x, CAVE_FLOOR + 1, z, "minecraft:red_mushroom" if hash2(x, z) < 0.5 else "minecraft:brown_mushroom")
            if hash2(x - 5, z + 9) < 0.006:
                world.set(x, ceiling, z, "minecraft:shroomlight")
    start, end = (240, -440, 76), (212, -478, CAVE_FLOOR)
    length = int(math.hypot(end[0] - start[0], end[1] - start[1]) * 3)
    for i in range(length + 1):
        t = i / length
        x = start[0] + (end[0] - start[0]) * t
        z = start[1] + (end[1] - start[1]) * t
        floor = int(round(start[2] + (end[2] - start[2]) * t))
        for ox in (-1, 0, 1):
            for oz in (-1, 0, 1):
                px, pz = int(math.floor(x)) + ox, int(math.floor(z)) + oz
                world.set(px, floor, pz, "minecraft:stone")
                world.fill(px, floor + 1, pz, px, floor + 4, pz, AIR)


def carve_lab(world):
    room(world, 168, 182, -484, -468, 26)
    room(world, 168, 180, -504, -494, 28)
    room(world, 186, 196, -506, -494, 22)
    room(world, 204, 212, -502, -492, 14)
    world.fill(172, 25, -494, 176, 31, -484, BRICK)
    world.fill(173, 26, -493, 175, 28, -485, AIR)
    world.fill(173, 28, -494, 175, 30, -494, AIR)
    world.fill(173, 28, -493, 175, 30, -493, AIR)
    world.fill(173, 27, -493, 175, 27, -493, BRICK)
    for x in (173, 174, 175):
        world.set(x, 26, -492, BRICK)
        world.fill(x, 27, -492, x, 29, -492, AIR)
        world.set(x, 27, -492, STAIRS_NORTH)
        world.set(x, 25, -491, BRICK)
        world.fill(x, 26, -491, x, 28, -491, AIR)
        world.set(x, 26, -491, STAIRS_NORTH)
    for x in range(181, 188):
        feet = max(22, 28 - (x - 181))
        world.fill(x, feet - 1, -501, x, feet + 3, -497, BRICK)
        world.fill(x, feet, -500, x, feet + 2, -498, AIR)
    for x in range(198, 206):
        feet = max(14, 20 - (x - 198))
        world.fill(x, feet - 1, -499, x, feet + 3, -495, BRICK)
        world.fill(x, feet, -498, x, feet + 2, -496, AIR)
    world.fill(197, 22, -498, 197, 23, -496, "minecraft:cracked_stone_bricks")
    world.fill(175, 31, -470, 175, 41, -470, AIR)
    world.set(175, 42, -470, "minecraft:coarse_dirt")


def carve_oasis(world):
    ox, oz, rx, rz = OASIS
    for x in range(int(ox - rx) - 2, int(ox + rx) + 3):
        for z in range(int(oz - rz) - 2, int(oz + rz) + 3):
            od = math.hypot((x - ox) / rx, (z - oz) / rz)
            if od < 0.85:
                world.fill(x, 71, z, x, SEA, z, "minecraft:water")
            if 0.7 < od < 1.15 and hash2(x, z) < 0.12:
                world.set(x, height(x, z) + 1, z, "minecraft:sugar_cane")


def carve_world(world, ramp):
    carve_cave(world)
    carve_lab(world)
    carve_oasis(world)


def column_heights(chunk_x, chunk_z, ramp):
    heights = {}
    for lx in range(16):
        for lz in range(16):
            x, z = chunk_x * 16 + lx, chunk_z * 16 + lz
            heights[(lx, lz)] = height(x, z)
    return heights


def build_chunk(chunk_x, chunk_z, world, ramp):
    heights = column_heights(chunk_x, chunk_z, ramp)
    sections = []
    for section in range(MIN_SECTION, MAX_SECTION + 1):
        if section < 0 or section > 12:
            sections.append(None)
            continue
        names = []
        any_block = False
        for ly in range(16):
            y = section * 16 + ly
            for lz in range(16):
                for lx in range(16):
                    x, z = chunk_x * 16 + lx, chunk_z * 16 + lz
                    block = world.blocks.get((x, y, z))
                    if block is None:
                        surface = heights[(lx, lz)]
                        ramp_y = ramp.get((x, z))
                        if y < 0:
                            block = "minecraft:air"
                        elif y == 0:
                            block = "minecraft:bedrock"
                        elif ramp_y is not None and surface < ramp_y and ramp_y - 6 <= y <= ramp_y:
                            block = "minecraft:stone_bricks" if y == ramp_y else "minecraft:stone"
                        elif ramp_y is not None and ramp_y < y <= ramp_y + 3:
                            block = "minecraft:air"
                        elif y <= surface:
                            block = surface_block(surface, surface - y)
                        else:
                            block = "minecraft:air"
                    if block != "minecraft:air":
                        any_block = True
                    names.append(block)
        sections.append(names if any_block else None)
    out = bytearray(var(chunk_x) + var(chunk_z))
    for names in sections:
        if names is None:
            out.append(1)
            continue
        out.append(0)
        palette = list(dict.fromkeys(names))
        out += var(len(palette))
        for entry in palette:
            out += string(entry)
        if len(palette) > 1:
            bits = max(1, math.ceil(math.log2(len(palette))))
            per = 64 // bits
            index = {name: i for i, name in enumerate(palette)}
            longs = []
            for start in range(0, 4096, per):
                value = 0
                for k, name in enumerate(names[start:start + per]):
                    value |= index[name] << (bits * k)
                longs.append(value)
            out += var(len(longs)) + struct.pack(">%dq" % len(longs), *[v - (1 << 64) if v >= 1 << 63 else v for v in longs])
        out += var(1) + string("minecraft:desert")
        out.append(1)
        out.append(1)
    out += var(0)
    out += struct.pack(">i", 0)
    out += var(0)
    return bytes(out)


def main(source, destination):
    header, data_version, body = split_polar(source)
    count_pos, count, chunks_pos = parse_chunk_coords(body)
    present = existing_chunks(body)
    ramp = ramp_cells()
    world = World()
    carve_world(world, ramp)
    new_chunks = []
    for cx in range(X_RANGE[0] // 16, X_RANGE[1] // 16 + 1):
        for cz in range(Z_RANGE[0] // 16, Z_RANGE[1] // 16 + 1):
            if (cx, cz) in present:
                continue
            new_chunks.append(build_chunk(cx, cz, world, ramp))
    merged = body[:count_pos] + var(count + len(new_chunks)) + body[chunks_pos:] + b"".join(new_chunks)
    compressed = zstd.compress(merged)
    open(destination, "wb").write(header + var(data_version) + bytes([1]) + var(len(merged)) + compressed)
    print("kept", count, "added", len(new_chunks), "bytes", len(compressed))


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
