# C3PO Auto-Update Testing Infrastructure

This directory contains testing tools for the C3PO auto-update functionality.

## Quick Start

1. **Start the mock server:**
   ```bash
   python3 test-server/mock_github_api.py
   ```

2. **Set test scenario:**
   ```bash
   ./test-server/test-scenarios.sh update
   ```

3. **Configure C3PO for testing:**
   Add to `~/.config/c3po/c3po.cfg`:
   ```
   update.check.url=http://localhost:8080/releases/latest
   ```

4. **Run C3PO to see the update dialog**

## Test Scenarios

- **`update`** - Shows update available (v2.1.0)
- **`no-update`** - Shows no update needed (v2.0.1)
- **`major`** - Shows major update (v3.0.0)
- **`status`** - Check server status

## Files

- `mock_github_api.py` - Flask server simulating GitHub API
- `test-scenarios.sh` - Helper script to change scenarios
- `README.md` - This documentation