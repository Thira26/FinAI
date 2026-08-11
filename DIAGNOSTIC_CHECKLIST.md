# FinAI Transaction Scanning Diagnostic Checklist

## Issues Fixed:
1. **Added proper error logging** in SMS scanning and real-time SMS processing
2. **Improved exception handling** to show actual error messages instead of silently failing
3. **Added debug logging** to track SMS reception and processing

## Steps to Diagnose Transaction Scanning Issues:

### 1. Check App Permissions
- Go to Settings > Apps > FinAI > Permissions
- Ensure SMS, Read SMS, and Receive SMS permissions are granted
- If not granted, grant them and restart the app

### 2. Test Manual SMS Scanning
- Open the app
- Go to Transactions tab
- Click "Scan SMS" button
- Check for any error messages that now appear

### 3. Check Real-time SMS Processing
- Send a test transaction SMS to your device
- Check Android logs: `adb logcat -s FinAI:*`
- Look for "SMS received" and "Processing SMS" messages

### 4. Common Issues & Solutions:

#### Issue: No permissions granted
**Solution**: Grant SMS permissions manually in device settings

#### Issue: SMS scanning returns 0 transactions
**Possible causes**:
- No transaction SMS in recent 200 messages
- SMS format not recognized by parser
- Android version restrictions (Android 11+ has stricter SMS access)

**Solution**: 
- Click "Add Sample Data" button to test UI functionality
- Check if your bank SMS format matches expected patterns

#### Issue: Real-time SMS not working
**Possible causes**:
- SMS receiver not registered properly
- Battery optimization killing the receiver
- Android Doze mode

**Solutions**:
- Add app to battery optimization whitelist
- Check if SMS receiver is enabled in AndroidManifest.xml

### 5. Test with Sample Data
- In Transactions tab, click "Add Sample Data" button
- This creates 16 sample transactions over 7 days
- Verify these appear in the UI

### 6. Check Logs for Errors
Run: `adb logcat -s FinAI:* | grep -E "(Error|Exception|Failed)"`
Look for any error messages in the output

## Expected SMS Patterns:
The parser looks for SMS containing:
- Amount patterns: ₹123, Rs.123, INR 123, Rs 123
- Merchant patterns: "at Swiggy", "from Uber", "paid to Amazon"
- Transaction indicators: "debited", "credited", "paid", "charged"

## If Still Not Working:
1. Check Android version compatibility
2. Verify SMS permissions are granted
3. Test with different bank SMS formats
4. Check if any other SMS apps are intercepting messages
5. Try restarting the device

## Build Status:
✅ App builds successfully
✅ Error handling improved
✅ Debug logging added
✅ Sample data functionality available
