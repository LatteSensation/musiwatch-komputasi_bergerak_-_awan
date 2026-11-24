# MusiWatch - Praktik 1

## Menjalankan API
cd api
python -m venv .venv
.venv\Scripts\activate        (Linux/macOS: source .venv/bin/activate)
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000

## Pengukuran curl
BASE=http://localhost:8000 HTTPS_URL=https://example.com N=10 bash scripts/measure_latency.sh > docs/p1_curl.csv
python scripts/median-max.py

## Aplikasi Android
Buka folder android/ di Android Studio, Run di emulator API 36 (API berjalan di port 8000).
Lembar kerja: docs/P1.md