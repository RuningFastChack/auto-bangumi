"""
Build supervised fine-tuning data from db/app.db.

The script reads distinct rss_item.torrent_name rows, applies deterministic
rules that match the Java OllamaParser output schema, then writes:

  traning/data/train.jsonl
  traning/data/val.jsonl

Each JSONL row has this shape:

  {"input": "<raw title>", "output": {"episode": "...", ...}}
"""

from __future__ import annotations

import argparse
import json
import random
import re
import sqlite3
from pathlib import Path
from typing import Iterable

BASE_DIR = Path(__file__).resolve().parent
PROJECT_DIR = BASE_DIR.parent
DEFAULT_DB_PATH = PROJECT_DIR / "db" / "app.db"
DEFAULT_OUTPUT_DIR = BASE_DIR / "data"

EPISODE_MAX = 300

RE_GROUP_AT_START = re.compile(r"^\s*[\[【]([^\]】]+)[\]】]")
RE_ANY_TAG = re.compile(r"[\[【]([^\]】]+)[\]】]")
RE_TAG_TOKEN = re.compile(r"[\[【][^\]】]+[\]】]")
RE_SPACES = re.compile(r"\s+")
RE_CJK = re.compile(r"[\u3400-\u4dbf\u4e00-\u9fff]")
RE_KANA = re.compile(r"[\u3040-\u30ff]")
RE_LATIN = re.compile(r"[A-Za-z]")
RE_LATIN_TEXT = re.compile(r"[A-Za-z][A-Za-z0-9\s!&':.,+()\-]+")

RE_DPI = re.compile(r"(?<!\d)(2160|1440|1080|720|480)[pP]?(?!\d)")
RE_RESOLUTION = re.compile(r"(?<!\d)\d{3,4}x(2160|1440|1080|720|480)(?!\d)", re.I)
RE_SOURCE = re.compile(r"\b(B-Global|Baha|Bilibili|ABEMA|CR|AT-X|ViuTV)\b", re.I)
RE_SOURCE_WEB = re.compile(r"\bWEB(?:-DL|Rip)?\b", re.I)

RE_EPISODE_CN = re.compile(r"第\s*([0-9一二三四五六七八九十两]+)\s*[集话話]")
RE_EPISODE_E = re.compile(r"(?<![A-Za-z])E(?:P)?\s*(\d{1,3})(?!\d)", re.I)
RE_EPISODE_RANGE_TAG = re.compile(r"^\s*(\d{1,3})\s*-\s*(\d{1,3})(?:\s*合集)?\s*$")
RE_EPISODE_NUM_TAG = re.compile(r"^\s*(\d{1,3})(?:v\d+)?\s*$", re.I)
RE_EPISODE_AFTER_DASH = re.compile(r"(?:^|[\s_./-])-\s*(\d{1,3})(?:v\d+)?(?=\s*(?:[\[【(]|$))")
RE_EPISODE_TRAILING = re.compile(r"(?:^|[\s._-])(\d{1,3})(?:v\d+)?(?=\s*(?:[\[【(]|$))")

RE_SEASON_EN = re.compile(r"\b(?:S|Season\s*)(\d{1,2})\b", re.I)
RE_SEASON_ORDINAL = re.compile(r"\b(\d{1,2})(?:st|nd|rd|th)\s+Season\b", re.I)
RE_SEASON_CN = re.compile(r"第\s*([0-9一二三四五六七八九十两]+)\s*季")
RE_SEASON_CN_SUFFIX = re.compile(r"([一二三四五六七八九十两0-9]+)\s*季")

RE_RELEASE_TAG = re.compile(r"^\s*[★☆\s]*(?:\d{1,2}月)?新番[★☆\s]*$", re.I)
RE_CODEC_TAG = re.compile(
    r"\b(MP4|MKV|AVC|HEVC|H\.?264|H\.?265|AAC|FLAC|WEB-DL|WEBRip|GB|BIG5|CHS|CHT|Fin|END)\b",
    re.I,
)
RE_SUB_CHS = re.compile(r"CHS|GB|简|簡|简体|簡體|简中|簡中")
RE_SUB_CHT = re.compile(r"CHT|BIG5|繁|繁体|繁體|繁中")
RE_SUB_JP = re.compile(r"日语|日文|日字|日双|日內|日内|日")

RE_CLEAN_EP_TAIL = re.compile(r"\s*(?:-|_|/)\s*\d{1,3}(?:v\d+)?\s*-?\s*$", re.I)
RE_CLEAN_SEASON = re.compile(
    r"\b(?:S|Season\s*)\d{1,2}\b|第\s*[0-9一二三四五六七八九十两]+\s*季|[一二三四五六七八九十两0-9]+\s*季",
    re.I,
)

CN_NUM = {
    "一": 1,
    "二": 2,
    "两": 2,
    "三": 3,
    "四": 4,
    "五": 5,
    "六": 6,
    "七": 7,
    "八": 8,
    "九": 9,
    "十": 10,
}


def clean_text(value: str) -> str:
    value = value.replace("　", " ")
    value = RE_SPACES.sub(" ", value)
    return value.strip()


def cn_num_to_int(value: str) -> int | None:
    value = value.strip()
    if value.isdigit():
        return int(value)
    if value == "十":
        return 10
    if value.startswith("十"):
        return 10 + CN_NUM.get(value[1:], 0)
    if value.endswith("十"):
        return CN_NUM.get(value[:-1], 0) * 10
    if "十" in value:
        left, right = value.split("十", 1)
        return CN_NUM.get(left, 1) * 10 + CN_NUM.get(right, 0)
    return CN_NUM.get(value)


def valid_episode(num: int) -> bool:
    return 0 < num <= EPISODE_MAX


def normalize_episode(num: str) -> str:
    value = int(num)
    return str(value) if valid_episode(value) else "NaN"


def extract_group(title: str) -> str:
    match = RE_GROUP_AT_START.search(title)
    return clean_text(match.group(1)) if match else ""


def strip_leading_groups(title: str) -> str:
    rest = title
    while True:
        new_rest = RE_GROUP_AT_START.sub("", rest, count=1).strip()
        if new_rest == rest:
            return rest.strip()
        rest = new_rest


def all_tags(title: str) -> list[str]:
    return [clean_text(match.group(1)) for match in RE_ANY_TAG.finditer(title)]


def is_episode_tag(tag: str) -> bool:
    range_match = RE_EPISODE_RANGE_TAG.match(tag)
    if range_match:
        return valid_episode(int(range_match.group(2)))
    num_match = RE_EPISODE_NUM_TAG.match(tag)
    return bool(num_match and valid_episode(int(num_match.group(1))))


def is_meta_tag(tag: str) -> bool:
    normalized = clean_text(tag)
    if not normalized:
        return True
    if is_episode_tag(normalized):
        return True
    if RE_RELEASE_TAG.match(normalized):
        return True
    if RE_DPI.search(normalized) or RE_RESOLUTION.search(normalized):
        return True
    if RE_SOURCE.search(normalized) or RE_SOURCE_WEB.search(normalized):
        return True
    if RE_CODEC_TAG.search(normalized):
        return True
    if RE_SUB_CHS.search(normalized) or RE_SUB_CHT.search(normalized):
        return True
    return False


def extract_episode(title: str) -> str:
    match = RE_EPISODE_CN.search(title)
    if match:
        value = cn_num_to_int(match.group(1))
        if value and valid_episode(value):
            return str(value)

    match = RE_EPISODE_E.search(title)
    if match:
        return normalize_episode(match.group(1))

    for tag in all_tags(title):
        range_match = RE_EPISODE_RANGE_TAG.match(tag)
        if range_match:
            return normalize_episode(range_match.group(2))

    for tag in all_tags(title):
        num_match = RE_EPISODE_NUM_TAG.match(tag)
        if num_match:
            return normalize_episode(num_match.group(1))

    match = RE_EPISODE_AFTER_DASH.search(title)
    if match:
        return normalize_episode(match.group(1))

    match = RE_EPISODE_TRAILING.search(title)
    if match:
        return normalize_episode(match.group(1))

    return "NaN"


def extract_season(title: str) -> int:
    for regex in (RE_SEASON_CN, RE_SEASON_CN_SUFFIX):
        match = regex.search(title)
        if match:
            value = cn_num_to_int(match.group(1))
            if value:
                return value

    match = RE_SEASON_EN.search(title)
    if match:
        return int(match.group(1))

    match = RE_SEASON_ORDINAL.search(title)
    if match:
        return int(match.group(1))

    return 1


def extract_dpi(title: str) -> str:
    match = RE_RESOLUTION.search(title)
    if match:
        return match.group(1)
    match = RE_DPI.search(title)
    return match.group(1) if match else ""


def extract_source(title: str) -> str:
    match = RE_SOURCE.search(title)
    if match:
        raw = match.group(1)
        canonical = {
            "b-global": "B-Global",
            "baha": "Baha",
            "bilibili": "Bilibili",
            "abema": "ABEMA",
            "cr": "CR",
            "at-x": "AT-X",
            "viutv": "ViuTV",
        }
        return canonical.get(raw.lower(), raw)
    if RE_SOURCE_WEB.search(title):
        return "Web"
    return ""


def extract_sub(title: str) -> str:
    if RE_SUB_CHT.search(title):
        return "CHT"
    if RE_SUB_CHS.search(title):
        return "CHS"
    if RE_SUB_JP.search(title):
        return "JPN"
    return ""


def clean_name_segment(segment: str) -> str:
    segment = clean_text(segment)
    segment = RE_TAG_TOKEN.sub(" ", segment)
    segment = re.sub(r"\([^)]*(?:检索|search|字幕|内封|内嵌|MP4|MKV|AVC|AAC|HEVC)[^)]*\)", " ", segment, flags=re.I)
    segment = RE_CLEAN_SEASON.sub("", segment)
    segment = RE_CLEAN_EP_TAIL.sub("", segment)
    segment = re.sub(r"^[★☆\s]*(?:\d{1,2}月)?新番[★☆\s]*", "", segment)
    segment = re.sub(r"[★☆]+", " ", segment)
    return clean_text(segment.strip(" -_/|:："))


def split_title_segments(value: str) -> list[str]:
    value = clean_name_segment(value)
    if not value:
        return []
    parts = re.split(r"\s*(?:/|／|\||丨)\s*", value)
    return [clean_name_segment(part) for part in parts if clean_name_segment(part)]


def title_prefix(rest: str) -> str:
    first_tag = RE_ANY_TAG.search(rest)
    prefix = rest[: first_tag.start()] if first_tag else rest
    prefix = re.split(r"\s[\[(]", prefix, 1)[0]
    return clean_name_segment(prefix)


def title_tags_after_group(title: str) -> list[str]:
    tags = all_tags(title)
    group = extract_group(title)
    if tags and group and tags[0] == group:
        tags = tags[1:]
    return tags


def collect_name_segments(title: str) -> list[str]:
    rest = strip_leading_groups(title)
    segments = split_title_segments(title_prefix(rest))
    if segments:
        return segments

    collected: list[str] = []
    for tag in title_tags_after_group(title):
        if is_episode_tag(tag):
            break
        if is_meta_tag(tag):
            continue
        collected.extend(split_title_segments(tag))

    return collected


def assign_names(segments: Iterable[str]) -> tuple[str, str, str]:
    name_zh = ""
    name_en = ""
    name_jp = ""

    for raw in segments:
        segment = clean_name_segment(raw)
        if not segment:
            continue

        has_kana = bool(RE_KANA.search(segment))
        has_cjk = bool(RE_CJK.search(segment))
        has_latin = bool(RE_LATIN.search(segment))

        if has_kana and len(segment) > len(name_jp):
            name_jp = segment
            continue

        if has_cjk and len(segment) > len(name_zh):
            name_zh = segment

        if has_latin and not has_cjk and len(segment) > len(name_en):
            name_en = segment
        elif has_latin and has_cjk and not name_en:
            latin = RE_LATIN_TEXT.search(segment)
            if latin:
                name_en = clean_name_segment(latin.group(0))

    return name_zh, name_en, name_jp


def parse_title(title: str) -> dict[str, object] | None:
    title = clean_text(title)
    if len(title) < 5:
        return None

    episode = extract_episode(title)
    if episode == "NaN":
        return None

    name_zh, name_en, name_jp = assign_names(collect_name_segments(title))
    if not any((name_zh, name_en, name_jp)):
        return None

    return {
        "episode": episode,
        "season": extract_season(title),
        "nameEn": name_en,
        "nameJp": name_jp,
        "nameZh": name_zh,
        "sub": extract_sub(title),
        "dpi": extract_dpi(title),
        "source": extract_source(title),
        "group": extract_group(title),
    }


def read_titles(db_path: Path) -> list[str]:
    if not db_path.exists():
        raise FileNotFoundError(f"database not found: {db_path}")

    conn = sqlite3.connect(db_path)
    try:
        rows = conn.execute(
            """
            SELECT DISTINCT torrent_name
            FROM rss_item
            WHERE deleted = 0
              AND torrent_name IS NOT NULL
              AND torrent_name != ''
            """
        ).fetchall()
    finally:
        conn.close()

    return sorted({clean_text(row[0]) for row in rows if clean_text(row[0])})


def write_jsonl(path: Path, rows: list[dict[str, object]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="\n") as f:
        for row in rows:
            f.write(json.dumps(row, ensure_ascii=False, separators=(",", ":")) + "\n")


def generate_training_data(db_path: Path, output_dir: Path, val_ratio: float, seed: int) -> None:
    titles = read_titles(db_path)

    samples: list[dict[str, object]] = []
    failed: list[str] = []
    for title in titles:
        parsed = parse_title(title)
        if parsed is None:
            failed.append(title)
            continue
        samples.append({"input": title, "output": parsed})

    if not samples:
        raise RuntimeError("no training samples were generated")

    random.seed(seed)
    random.shuffle(samples)

    val_count = max(1, int(len(samples) * val_ratio))
    val_samples = samples[:val_count]
    train_samples = samples[val_count:]

    write_jsonl(output_dir / "train.jsonl", train_samples)
    write_jsonl(output_dir / "val.jsonl", val_samples)

    print(f"Raw titles: {len(titles)}")
    print(f"Generated samples: {len(samples)}")
    print(f"Skipped titles: {len(failed)}")
    print(f"Train: {len(train_samples)} -> {output_dir / 'train.jsonl'}")
    print(f"Val: {len(val_samples)} -> {output_dir / 'val.jsonl'}")
    print("\nSample rows:")
    for sample in samples[:5]:
        print(json.dumps(sample, ensure_ascii=False))


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Extract bangumi parser SFT data from app.db")
    parser.add_argument("--db", type=Path, default=DEFAULT_DB_PATH)
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_OUTPUT_DIR)
    parser.add_argument("--val-ratio", type=float, default=0.05)
    parser.add_argument("--seed", type=int, default=42)
    return parser.parse_args()


if __name__ == "__main__":
    args = parse_args()
    generate_training_data(args.db, args.output_dir, args.val_ratio, args.seed)
