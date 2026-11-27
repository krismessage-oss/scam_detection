import json
import re

SCAM_PATTERNS = [
    "verify your account", "account suspended", "send your password",
    "send your otp", "your account is blocked", "urgent payment",
    "bank details", "claim your prize", "account will be closed",
    "click here to claim"
]
SPAM_PATTERNS = [
    "promo", "discount", "limited offer", "free gift",
    "buy now", "subscribe", "sale", "cash prize"
]

def analyze_message(message):
    text = (message or "").lower()
    scam_hits = [p for p in SCAM_PATTERNS if p in text]
    spam_hits = [p for p in SPAM_PATTERNS if p in text]
    reasons = scam_hits + spam_hits
    has_link = bool(re.search(r"https?://|www\.", text))
    if has_link:
        reasons.append("Contains a web link")

    scam_score = len(scam_hits) * 2 + (1 if has_link else 0)
    spam_score = len(spam_hits)

    if scam_score >= 3:
        category, confidence = "Scam", min(95, 70 + scam_score * 5)
    elif spam_score >= 2:
        category, confidence = "Spam", min(90, 65 + spam_score * 5)
    else:
        category, confidence = "Review", 0
        if not reasons:
            reasons = ["No reliable determination; manual review required"]

    return {"category": category, "confidence": confidence, "reasons": reasons}

def analyze_json(message):
    return json.dumps(analyze_message(message))
