import csv, statistics as st
from collections import defaultdict
rows = defaultdict(list)
for r in csv.DictReader(open("docs/p1_curl.csv")):
    k = r["kondisi"]
    if k == "d_https":
        k += "_baru" if r["koneksi_baru"] == "1" else "_pakai_ulang"
    rows[k].append(r)
for k, rs in rows.items():
    out = [k]
    for col in ["dns", "conn", "tls", "ttfb", "total"]:
        v = [float(x[col]) * 1000 for x in rs]
        out.append(f"{col} {st.median(v):.1f}/{max(v):.1f} ms")
    s = [int(x["size"]) for x in rs]
    out.append(f"size {st.median(s):.0f}/{max(s)} B")
    print(" | ".join(out))