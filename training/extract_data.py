# -*- coding: utf-8 -*-
"""
从 app.db 提取 rss_item.torrent_name，正则解析标签，生成训练数据

用法（本地运行）：
  python extract_data.py
输出：
  data/real_train.json, data/real_val.json, data/real_test.json
"""

import sqlite3
import json
import re
import random
from pathlib import Path

BASE = Path(__file__).parent
DB_PATH = BASE.parent / "db" / "app.db"
DATA_DIR = BASE / "data"
DATA_DIR.mkdir(parents=True, exist_ok=True)


def load_titles():
    """从 SQLite 读取所有 torrent_name"""
    conn = sqlite3.connect(str(DB_PATH))
    cur = conn.cursor()
    cur.execute("SELECT DISTINCT torrent_name FROM rss_item WHERE torrent_name IS NOT NULL AND torrent_name != ''")
    rows = cur.fetchall()
    conn.close()
    return [r[0] for r in rows]


def parse_title(title):
    """正则解析标题，返回 {input, output}"""
    title = title.strip()

    # ── 已知字幕组 ──
    groups = [
        "喵萌奶茶屋", "LoliHouse", "桜都字幕组", "VCB-Studio", "ReinForce",
        "ANK-Raws", "Moozzi2", "Snow-Raws", "Beatrice-Raws", "LowPower-Raws",
        "UHA-WINGS", "NC-Raws", "Nekomoe kissaten", "离谱Sub", "北宇治字幕组",
        "千夏字幕组", "极影字幕社", "悠哈璃羽字幕社", "漫猫字幕组", "轻之国度",
        "幻樱字幕组", "風車字幕組", "豌豆字幕組", "枫雪动漫", "风之圣殿",
        "SweetSub", "jsum", "IrizaRaws", "YamiSora", "KissSub", "Mabors-Sub",
        "ANi", "Magic-Raws", "AI-Raws", "Sakurato", "Airota", "HYSUB",
    ]

    # ── 字幕类型 ──
    sub_map = {
        "简": "简", "繁": "繁", "CHS": "简", "CHT": "繁",
        "简体": "简", "繁體": "繁", "JPtc": "繁",
        "日语": "日", "日文": "日", "无字幕": "无",
    }

    # ── 分辨率 ──
    dpi_pattern = r"(?:\[|【|\(|（|\s)?(\d{3,4})(?:p|P|×\d+[pP]?)(?:\]|】|\)|）|\s)?"
    dpi_match = re.search(dpi_pattern, title)
    dpi = dpi_match.group(1) if dpi_match and 480 <= int(dpi_match.group(1)) <= 8640 else ""

    # ── 片源 ──
    source_patterns = [
        r"(?i)\b(Web|WebRip|BDRip|Baha|Bilibili|AT-X|BS11|TV|DVD|BluRay|Blu-ray|UHD|4K|Remux|HDR)\b",
        r"(?i)\[(Web|WebRip|BDRip|Baha|Bilibili|AT-X|TV)\b",
    ]
    source = ""
    for pat in source_patterns:
        sm = re.search(pat, title)
        if sm:
            s = sm.group(1).replace("-", "")
            source = {"webrip": "Web", "bdrip": "BDRip", "bluray": "BDRip"}.get(s.lower(), s)
            if source:
                break

    # ── 剧集数 ──
    episode = "0"
    ep_patterns = [
        r"(?<!\d)(?:\b|(?<![a-zA-Z]))[ \-]?(?:E[eE][pP]?)?[  ]*(\d{1,3}(?:\.\d)?)\s*(?:[END|Fin]|END|Fin|end|fin|完结|合集|v\d)?(?!\d|\.\d|[a-zA-Z])",
        r"[\-\s【\[]\s*#?(\d{1,3}(?:\.\d)?)\s*[\-\s】\]]",
        r"第\s*(\d{1,3}(?:\.\d)?)\s*[话話集]",
        r"\[(\d{1,3}(?:\.\d)?)(?:\]|\s*END|\s*Fin)",
        r"\-(\d{1,3})\b",
    ]
    for pat in ep_patterns:
        em = re.search(pat, title)
        if em:
            ep = em.group(1)
            try:
                f = float(ep)
                if 0 <= f <= 999:
                    episode = ep
                    break
            except ValueError:
                pass

    # ── 季数 ──
    season = 1
    s_patterns = [
        r"(?i)\bs(?:eason\s*)?(\d{1,2})\b",
        r"第\s*(\d{1,2})\s*季",
        r"Season\s*(\d{1,2})",
        r"\[S(\d{1,2})\b",
    ]
    for pat in s_patterns:
        sm = re.search(pat, title)
        if sm:
            s = int(sm.group(1))
            if 1 <= s <= 99:
                season = s
                break

    # ── 字幕类型 ──
    sub = ""
    for k, v in sub_map.items():
        if k in title:
            sub = v
            break

    # ── 字幕组 ──
    group = ""
    for g in groups:
        if g in title:
            group = g
            break

    # ── 标题 ──
    # 中文标题
    zh_patterns = [
        r"(?<=\])([^\[\]\(\)（）]+?)(?=\s*[\[（(])",
        r"【(.+?)】",
        r"《(.+?)》",
        r"^(.+?)(?=\s*[\[【(（])",
        r"^(.+?)(?:\s*[Ee]\d|\s*\d{2})",
    ]
    name_zh = ""
    for pat in zh_patterns:
        zm = re.search(pat, title)
        if zm:
            name_zh = zm.group(1).strip()
            if len(name_zh) >= 2 and re.search(r"[\u4e00-\u9fff]", name_zh):
                break
            name_zh = ""

    # 英文标题
    en_patterns = [
        r"(?<=\])\s*([A-Za-z][^\[\]\(\)（）\u4e00-\u9fff]+?)(?=\s*[\[（({])",
        r"(?<=[/\s])\b([A-Z][a-z]+(?:\s+[A-Z][a-z]+)+)\b",
        r"\b([A-Z][a-zA-Z]{2,}(?:\s+[A-Z][a-zA-Z]{2,})*)\b",
    ]
    name_en = ""
    for pat in en_patterns:
        em = re.search(pat, title)
        if em:
            candidate = em.group(1).strip()
            if re.search(r"[A-Z]", candidate) and candidate.lower() not in {"web", "bdrip", "end", "fin", "tv", "hdr", "atx"}:
                name_en = candidate
                break

    output = json.dumps({
        "episode": episode,
        "season": season,
        "nameEn": name_en,
        "nameJp": "",
        "nameZh": name_zh,
        "sub": sub,
        "dpi": dpi,
        "source": source,
        "group": group,
    }, ensure_ascii=False)

    return {"input": title, "output": output}


def synthesize_samples():
    """基于模板生成补充合成数据，增强模型泛化能力"""
    titles = {}
    groups_short = [
        "喵萌", "桜都", "LoliHouse", "NC-Raws", "ANi", "SweetSub",
        "VCB-Studio", "Moozzi2", "UHA-WINGS", "北宇治",
    ]

    templates = [
        ("[{}] 进击的巨人 The Final Season / Shingeki no Kyojin - {:02d} END [WebRip 1080p HEVC-10bit AAC][{}]", 2),
        ("[{}] 辉夜大小姐想让我告白 / Kaguya-sama wa Kokurasetai S{} - {:02d} [1080p][{}]", 3),
        ("[{}&LoliHouse] 葬送的芙莉莲 / Sousou no Frieren - {:02d} [WebRip 1080p HEVC-10bit AAC ASSx2]", 1),
        ("[{}] 我推的孩子 / Oshi no Ko - {:02d} [Baha][WebDL 1080p AVC AAC][{}]", 2),
        ("[{}] 咒术回战 S{} / Jujutsu Kaisen - {:02d} [BDRip 1080p HEVC FLAC]", 2),
        ("[{}] 孤独摇滚 / Bocchi the Rock! - {:02d} [Baha][WebRip 1080p][{}]", 1),
        ("[{}] 间谍过家家 S{} / Spy x Family - {:02d} [Bilibili][1080p AVC][{}]", 2),
        ("[{}] 赛博朋克：边缘行者 / Cyberpunk Edgerunners - {:02d} [WebRip 1080p][{}]", 1),
        ("[{}] 更衣人偶坠入爱河 / Sono Bisque Doll wa Koi wo Suru - {:02d} [BDRip][{}]", 1),
        ("[{}] 无职转生 S{} / Mushoku Tensei - {:02d} [WebRip 1080p]", 2),
        ("[{}] 天国大魔境 / Tengoku Daimakyou - {:02d} [Baha][1080p][{}]", 1),
        ("[{}] 冰海战记 S{} / Vinland Saga - {:02d} [WebRip 1080p HEVC][{}]", 2),
        ("[{}] 灵能百分百 S{} / Mob Psycho - {:02d} [BDRip 1080p][{}]", 3),
        ("[{}] 文豪野犬 S{} / Bungo Stray Dogs - {:02d} [WebRip 720p][{}]", 4),
        ("[{}] 五等分的花嫁 ∬ / 5-toubun no Hanayome ∬ - {:02d} [Baha][1080p][{}]", 2),
        ("[{}] 摇曳露营 S{} / Yuru Camp - {:02d} [Bilibili][1080p][{}]", 2),
        ("[{}] 关于我转生变成史莱姆这档事 S{} / Tensei Shitara Slime - {:02d} [WebRip 1080p][{}]", 3),
        ("[{}] 鬼灭之刃 游郭篇 / Kimetsu no Yaiba S2 - {:02d} [Baha][1080p][{}]", 2),
        ("[{}] 86-不存在的战区- Part2 / 86 EIGHTY-SIX - {:02d} [WebRip 1080p][{}]", 2),
        ("[{}] 夏日重现 / Summer Time Render - {:02d} [Bilibili][1080p AVC][{}]", 1),
    ]

    for tpl, default_n in templates:
        group = random.choice(groups_short)
        ep = random.randint(1, default_n)
        season = 1
        # 解析模板中的 S{}
        if "S{}" in tpl:
            season = 2 if "S2" in tpl else default_n
        title = tpl.format(group, season, ep, "简繁内嵌" if random.random() > 0.5 else "简")
        if title not in titles:
            titles[title] = parse_title(title)

    return list(titles.values())


def main():
    print("=" * 60)
    print("Phase 1: 从 DB 提取真实标题")
    print("=" * 60)
    all_titles = load_titles()
    print(f"从 app.db 读取了 {len(all_titles)} 条 torrent_name")

    real_data = []
    for t in all_titles:
        item = parse_title(t)
        if item["output"]:
            real_data.append(item)

    print(f"解析成功: {len(real_data)} 条")

    print("\nPhase 2: 生成合成数据")
    synth_data = synthesize_samples()
    print(f"合成: {len(synth_data)} 条")

    print("\nPhase 3: 合并、去重、分割")
    seen = set()
    all_data = []
    for item in real_data + synth_data:
        inp = item["input"]
        if inp and inp not in seen:
            seen.add(inp)
            all_data.append(item)

    random.shuffle(all_data)
    n = len(all_data)
    train = all_data[:int(n * 0.8)]
    val = all_data[int(n * 0.8):int(n * 0.9)]
    test = all_data[int(n * 0.9):]

    for name, data in [("train", train), ("val", val), ("test", test)]:
        path = DATA_DIR / f"real_{name}.json"
        with open(path, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        print(f"  real_{name}.json: {len(data)} 条")

    # 也输出一个合并版
    merged_path = DATA_DIR / "merged_all.json"
    with open(merged_path, "w", encoding="utf-8") as f:
        json.dump(all_data, f, ensure_ascii=False, indent=2)

    print(f"\n总计: {n} 条 → {merged_path}")
    print("\n完成！把 data/ 目录打包上传到 PAI-DSW。")


if __name__ == "__main__":
    main()
