# Nova Guide Backend

Small Flask gateway for the Android app's Nova Guide feature.

The Android app calls this backend, and this backend calls the LLM provider.
This keeps `GROQ_API_KEY` out of the APK and out of git.

## Run Locally

```powershell
cd nova-guide-backend
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
$env:GROQ_API_KEY="gsk-your-key"
python app.py
```

You can also create a local `.env` file in this folder:

```properties
GROQ_API_KEY=gsk-your-key
GROQ_MODEL=openai/gpt-oss-20b
PORT=5050
```

For the Android emulator, set this in Android `local.properties`:

```properties
NOVA_GUIDE_ENDPOINT=http://10.0.2.2:5050/v1/apod-guide
```

Use `10.0.2.2` for the Android emulator. For a physical phone, put the computer
and phone on the same Wi-Fi network, then use the computer's LAN IP:

```properties
NOVA_GUIDE_ENDPOINT=http://YOUR_LAN_IP:5050/v1/apod-guide
```

The debug Android build allows cleartext local-network traffic for this testing
flow. For release-like testing, deploy this backend to HTTPS.

## API

`POST /v1/apod-guide`

Request:

```json
{
  "date": "2026-04-14",
  "title": "The Long Wispy Tail of Comet R3 (PanSTARRS)",
  "explanation": "NASA APOD explanation text",
  "imageUrl": "https://apod.nasa.gov/..."
}
```

Response:

```json
{
  "shortSummary": "彗星的細長尾巴來自太陽作用。",
  "plainChinese": "這張圖展示彗星 R3 的細長尾巴。彗星靠近太陽時，氣體與塵埃會被推出去，形成尾巴。",
  "keyPoints": ["主角是彗星 R3", "尾巴來自氣體與塵埃", "方向受太陽影響"],
  "terms": [
    {
      "term": "solar wind",
      "zh": "太陽風",
      "explanation": "太陽吹出的帶電粒子流。"
    }
  ],
  "source": "NASA APOD explanation"
}
```
