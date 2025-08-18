#!/bin/bash
# C3PO Bulk Package Information Extractor
# Usage: ./bulk-package-info.sh [package_filter]
# Example: ./bulk-package-info.sh com.android  (for system apps only)

PACKAGE_FILTER=${1:-""}

echo "=== C3PO Package Information Extractor ==="
echo "Filter: ${PACKAGE_FILTER:-"All packages"}"
echo "=========================================="

# Get list of packages
if [ -n "$PACKAGE_FILTER" ]; then
    PACKAGES=$(adb shell pm list packages | grep "$PACKAGE_FILTER" | cut -d: -f2)
else
    PACKAGES=$(adb shell pm list packages | cut -d: -f2)
fi

PACKAGE_COUNT=$(echo "$PACKAGES" | wc -l)
echo "Found $PACKAGE_COUNT packages to analyze..."
echo ""

COUNTER=1
for PACKAGE in $PACKAGES; do
    echo "[$COUNTER/$PACKAGE_COUNT] Processing: $PACKAGE"
    
    # Services (from dumpsys package - Service Resolver Table section)
    SERVICES=$(adb shell dumpsys package "$PACKAGE" | grep -A 1000 "Service Resolver Table" | grep -B 1000 "Receiver Resolver Table\|ContentProvider Resolver Table\|^\$" | grep "^ *[a-f0-9]\+.*$PACKAGE" | wc -l)
    
    # Sleep State (standby bucket)
    SLEEP_STATE=$(adb shell dumpsys usagestats "$PACKAGE" 2>/dev/null | head -1 | grep -o "standbyBucket=[0-9]\+" | cut -d= -f2)
    SLEEP_STATUS=""
    case $SLEEP_STATE in
        5) SLEEP_STATUS="EXEMPTED (system)" ;;
        10) SLEEP_STATUS="ACTIVE" ;;
        20) SLEEP_STATUS="WORKING_SET" ;;
        30) SLEEP_STATUS="FREQUENT" ;;
        40) SLEEP_STATUS="RARE" ;;
        50) SLEEP_STATUS="NEVER (restricted)" ;;
        *) SLEEP_STATUS="Unknown ($SLEEP_STATE)" ;;
    esac
    
    # Signature Hash
    SIGNATURE=$(adb shell dumpsys package "$PACKAGE" | grep "signatures=\[" | head -1 | grep -o "\[.*\]" | tr -d "[]")
    
    # Output results
    echo "  Services: $SERVICES declared"
    echo "  Sleep State: ${SLEEP_STATUS:-"No data"}"
    echo "  Signature: ${SIGNATURE:-"Not found"}"
    echo ""
    
    ((COUNTER++))
done

echo "Analysis complete for $PACKAGE_COUNT packages."