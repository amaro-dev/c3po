#!/bin/bash

# Test scenarios helper script for C3PO auto-update testing
# Usage: ./test-scenarios.sh [scenario_name]

BASE_URL="http://localhost:8080"

if [ $# -eq 0 ]; then
    echo "🔧 C3PO Auto-Update Test Scenarios"
    echo "=================================="
    echo
    echo "Usage: $0 <scenario>"
    echo
    echo "Available scenarios:"
    echo "  update       - Simulate update available (v2.1.0)"
    echo "  no-update    - Simulate no update needed (v2.0.1)" 
    echo "  major        - Simulate major update (v3.0.0)"
    echo "  status       - Show current server status"
    echo
    echo "Examples:"
    echo "  $0 update      # Set to show update available"
    echo "  $0 no-update   # Set to show no update"
    echo "  $0 status      # Check server status"
    echo
    exit 1
fi

SCENARIO=$1

case $SCENARIO in
    "update")
        echo "🔄 Setting scenario: Update Available (v2.1.0)"
        curl -s "$BASE_URL/set-scenario/update_available" | jq .
        ;;
    "no-update")
        echo "✅ Setting scenario: No Update (v2.0.1)"
        curl -s "$BASE_URL/set-scenario/no_update" | jq .
        ;;
    "major")
        echo "🚀 Setting scenario: Major Update (v3.0.0)"
        curl -s "$BASE_URL/set-scenario/major_update" | jq .
        ;;
    "status")
        echo "📊 Server Status:"
        curl -s "$BASE_URL/status" | jq .
        ;;
    *)
        echo "❌ Unknown scenario: $SCENARIO"
        echo "Run '$0' without arguments to see available scenarios."
        exit 1
        ;;
esac