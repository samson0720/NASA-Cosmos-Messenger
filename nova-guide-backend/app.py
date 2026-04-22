import json
import os
import urllib.error
import urllib.request

from flask import Flask, jsonify, request


GROQ_CHAT_COMPLETIONS_URL = "https://api.groq.com/openai/v1/chat/completions"

app = Flask(__name__)


@app.post("/v1/apod-guide")
def create_apod_guide():
    payload = request.get_json(silent=True) or {}
    validation_error = _validate_payload(payload)
    if validation_error:
        return jsonify({"error": validation_error}), 400

    api_key = os.environ.get("GROQ_API_KEY", "").strip()
    if not api_key:
        return jsonify({"error": "GROQ_API_KEY is not configured"}), 503

    try:
        guide = _request_groq(api_key, payload)
    except urllib.error.HTTPError as exc:
        return jsonify({"error": _read_http_error(exc)}), 502
    except urllib.error.URLError as exc:
        return jsonify({"error": f"Unable to reach LLM provider: {exc.reason}"}), 502
    except (KeyError, ValueError, json.JSONDecodeError) as exc:
        return jsonify({"error": f"Invalid LLM response: {exc}"}), 502

    return jsonify(guide)


@app.get("/health")
def health():
    return jsonify({"status": "ok"})


def _validate_payload(payload):
    for key in ("date", "title", "explanation", "imageUrl"):
        value = payload.get(key)
        if not isinstance(value, str) or not value.strip():
            return f"{key} is required"
    if len(payload["explanation"]) > 6000:
        return "explanation is too long"
    return None


def _load_dotenv():
    env_path = os.path.join(os.path.dirname(__file__), ".env")
    if not os.path.exists(env_path):
        return

    with open(env_path, "r", encoding="utf-8") as env_file:
        for raw_line in env_file:
            line = raw_line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            normalized_key = key.strip().lstrip("\ufeff")
            os.environ.setdefault(normalized_key, value.strip().strip('"').strip("'"))


def _request_groq(api_key, apod):
    body = {
        "model": os.environ.get("GROQ_MODEL", "openai/gpt-oss-20b"),
        "messages": [
            {
                "role": "system",
                "content": (
                    "You are Nova, a concise astronomy guide inside a mobile APOD chat app. "
                    "Explain only facts supported by the NASA APOD title and explanation. "
                    "Write Traditional Chinese for a curious high-school reader. "
                    "Keep the answer compact enough for a phone bottom sheet. "
                    "Return exactly 3 keyPoints and at most 3 terms. "
                    "For each term, term must be the original English NASA phrase "
                    "and zh must be its Traditional Chinese translation."
                ),
            },
            {
                "role": "user",
                "content": (
                    f"Date: {apod['date']}\n"
                    f"Title: {apod['title']}\n"
                    f"NASA explanation:\n{apod['explanation']}\n"
                    "Return a structured APOD guide."
                ),
            }
        ],
        "response_format": {
            "type": "json_schema",
            "json_schema": {
                "name": "nova_apod_guide",
                "strict": False,
                "schema": {
                    "type": "object",
                    "additionalProperties": False,
                    "required": [
                        "shortSummary",
                        "plainChinese",
                        "keyPoints",
                        "terms",
                        "source",
                    ],
                    "properties": {
                        "shortSummary": {
                            "type": "string",
                            "description": "At most 35 Traditional Chinese characters.",
                        },
                        "plainChinese": {
                            "type": "string",
                            "description": "At most 120 Traditional Chinese characters.",
                        },
                        "keyPoints": {
                            "type": "array",
                            "items": {
                                "type": "string",
                                "description": "At most 24 Traditional Chinese characters.",
                            },
                        },
                        "terms": {
                            "type": "array",
                            "items": {
                                "type": "object",
                                "additionalProperties": False,
                                "required": ["term", "zh", "explanation"],
                                "properties": {
                                    "term": {"type": "string"},
                                    "zh": {
                                        "type": "string",
                                        "description": "Traditional Chinese translation of term.",
                                    },
                                    "explanation": {
                                        "type": "string",
                                        "description": "At most 40 Traditional Chinese characters.",
                                    },
                                },
                            },
                        },
                        "source": {
                            "type": "string",
                            "enum": ["NASA APOD explanation"],
                        },
                    },
                },
            }
        },
        "temperature": 0.2,
        "reasoning_effort": "low",
        "max_completion_tokens": 1200,
    }
    data = json.dumps(body).encode("utf-8")
    req = urllib.request.Request(
        GROQ_CHAT_COMPLETIONS_URL,
        data=data,
        headers={
            "Authorization": f"Bearer {api_key}",
            "Accept": "application/json",
            "Content-Type": "application/json",
            "User-Agent": "nova-guide-backend/0.1",
        },
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=30) as response:
        response_body = json.loads(response.read().decode("utf-8"))

    output_text = _extract_message_content(response_body)
    guide = json.loads(output_text)
    _validate_guide(guide)
    return guide


def _extract_message_content(response_body):
    return response_body["choices"][0]["message"]["content"]


def _validate_guide(guide):
    required = ("shortSummary", "plainChinese", "keyPoints", "terms", "source")
    for key in required:
        if key not in guide:
            raise ValueError(f"missing {key}")
    if not isinstance(guide["keyPoints"], list) or len(guide["keyPoints"]) != 3:
        raise ValueError("keyPoints must contain exactly 3 items")
    if not isinstance(guide["terms"], list):
        raise ValueError("terms must be a list")
    return guide


def _read_http_error(exc):
    try:
        details = exc.read().decode("utf-8")
    except Exception:
        details = str(exc)
    return f"LLM provider returned HTTP {exc.code}: {details}"


_load_dotenv()


if __name__ == "__main__":
    port = int(os.environ.get("PORT", "5050"))
    app.run(host="0.0.0.0", port=port)
