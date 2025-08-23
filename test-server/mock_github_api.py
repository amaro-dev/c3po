#!/usr/bin/env python3
"""
Mock GitHub API server for testing C3PO auto-update functionality.

This server simulates GitHub's releases API to test update notifications
without requiring actual GitHub releases or internet connectivity.

Usage:
    python3 mock_github_api.py

Then configure C3PO with:
    update.check.url=http://localhost:8080/releases/latest
"""

from flask import Flask, jsonify
import json
from datetime import datetime

app = Flask(__name__)

# Default scenario: update available
current_scenario = "update_available"

# Test scenarios
SCENARIOS = {
    "update_available": {
        "tag_name": "v2.1.0",
        "name": "C3PO 2.1.0 - Enhanced Android Explorer",
        "body": "## What's New\n- Auto-update functionality\n- Improved performance\n- Bug fixes and stability improvements",
        "published_at": "2024-08-22T12:00:00Z",
        "html_url": "https://github.com/amaro-dev/c3po/releases/tag/v2.1.0",
        "assets": [
            {
                "name": "c3po-2.1.0.dmg",
                "content_type": "application/octet-stream",
                "size": 23456789,
                "browser_download_url": "http://localhost:8080/download/c3po-2.1.0.dmg"
            }
        ]
    },
    "no_update": {
        "tag_name": "v2.0.1",
        "name": "C3PO 2.0.1 - Current Version",
        "body": "## Current Version\n- This is the current version, no update needed",
        "published_at": "2024-08-15T10:00:00Z",
        "html_url": "https://github.com/amaro-dev/c3po/releases/tag/v2.0.1",
        "assets": [
            {
                "name": "c3po-2.0.1.dmg",
                "content_type": "application/octet-stream",
                "size": 22345678,
                "browser_download_url": "http://localhost:8080/download/c3po-2.0.1.dmg"
            }
        ]
    },
    "major_update": {
        "tag_name": "v3.0.0",
        "name": "C3PO 3.0.0 - Major Release",
        "body": "## 🚀 Major Update Available!\n- Complete UI redesign\n- New plugin architecture\n- Performance improvements\n- Breaking changes - please read migration guide",
        "published_at": "2024-08-25T14:30:00Z",
        "html_url": "https://github.com/amaro-dev/c3po/releases/tag/v3.0.0",
        "assets": [
            {
                "name": "c3po-3.0.0.dmg",
                "content_type": "application/octet-stream",
                "size": 28901234,
                "browser_download_url": "http://localhost:8080/download/c3po-3.0.0.dmg"
            }
        ]
    }
}

@app.route('/releases/latest')
def get_latest_release():
    """Simulate GitHub's /repos/owner/repo/releases/latest endpoint"""
    return jsonify(SCENARIOS[current_scenario])

@app.route('/set-scenario/<scenario>')
def set_scenario(scenario):
    """Change the current test scenario"""
    global current_scenario
    if scenario in SCENARIOS:
        current_scenario = scenario
        return jsonify({
            "status": "success", 
            "scenario": scenario,
            "message": f"Scenario changed to: {scenario}"
        })
    else:
        return jsonify({
            "status": "error", 
            "message": f"Unknown scenario: {scenario}. Available: {list(SCENARIOS.keys())}"
        }), 400

@app.route('/scenario')
def get_current_scenario():
    """Get the current test scenario"""
    return jsonify({
        "current_scenario": current_scenario,
        "available_scenarios": list(SCENARIOS.keys()),
        "scenario_data": SCENARIOS[current_scenario]
    })

@app.route('/download/<filename>')
def download_file(filename):
    """Simulate file download endpoint (returns mock response)"""
    return f"Mock download of {filename} - this would be the actual DMG file", 200, {
        'Content-Type': 'application/octet-stream',
        'Content-Disposition': f'attachment; filename={filename}'
    }

@app.route('/status')
def server_status():
    """Server health check"""
    return jsonify({
        "status": "running",
        "current_scenario": current_scenario,
        "timestamp": datetime.now().isoformat(),
        "endpoints": [
            "/releases/latest - GitHub releases API simulation",
            "/set-scenario/<name> - Change test scenario", 
            "/scenario - Get current scenario",
            "/download/<file> - Simulate DMG download",
            "/status - This status endpoint"
        ]
    })

if __name__ == '__main__':
    print("🚀 Starting C3PO Auto-Update Mock Server")
    print("=" * 50)
    print(f"📡 Server: http://localhost:8080")
    print(f"📋 Current scenario: {current_scenario}")
    print(f"🔗 Test endpoint: http://localhost:8080/releases/latest")
    print(f"⚙️  Status: http://localhost:8080/status")
    print("=" * 50)
    print("🔧 Configure C3PO with: update.check.url=http://localhost:8080/releases/latest")
    print()
    
    app.run(host='localhost', port=8080, debug=True)