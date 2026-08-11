package com.example.finai.utils;

import com.example.finai.model.TransactionModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsParser {

    // Enhanced patterns for transaction detection
    private static final Pattern AMOUNT = Pattern.compile("(?:INR|Rs\\.?|₹|Rs)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern MERCHANT_PATTERN = Pattern.compile(
        "(?:at|from|to|via|for|paid to|paid at|spent at|spent on|purchase at|purchase from|order at|order from|payment to|payment at)\\s*([A-Za-z0-9][A-Za-z0-9 .&'\"-]{3,40})(?=\\s|\\.|,|;|$)", 
        Pattern.CASE_INSENSITIVE
    );
    
    // Common non-expense indicators
    private static final String[] NON_EXPENSE_KEYWORDS = {
        "OTP", "verification", "missed call", "missedcall", "airtel thanks", "jio", "balance", "due date",
        "your account", "password", "login", "alert", "upi id", "upi pin", "upi request", "upi collect"
    };

    public static TransactionModel parse(String message, long timestamp) {
        if (message == null) return null;
        String msg = message.trim();
        String lowMsg = msg.toLowerCase(Locale.US);
        
        // Skip non-expense related messages
        for (String keyword : NON_EXPENSE_KEYWORDS) {
            if (lowMsg.contains(keyword.toLowerCase(Locale.US))) {
                return null;
            }
        }

        double amount = 0;
        Matcher ma = AMOUNT.matcher(msg);
        if (ma.find()) {
            String amtStr = ma.group(1).replace(",", "");
            try { amount = Double.parseDouble(amtStr); } catch (Exception ignored) {}
        }

        // Extract merchant with improved pattern matching
        String merchant = null;
        Matcher merchantMatcher = MERCHANT_PATTERN.matcher(msg);
        if (merchantMatcher.find()) {
            merchant = merchantMatcher.group(1).trim();
            
            // Clean up merchant name
            merchant = merchant.replaceAll("^[^a-zA-Z0-9]+", "")  // Remove leading special chars
                             .replaceAll("[^a-zA-Z0-9 ]+$", "")  // Remove trailing special chars
                             .replaceAll("\\s+", " ")            // Normalize spaces
                             .trim();
            
            // Common merchant name corrections
            merchant = merchant.replaceAll("(?i)swiggy.*", "Swiggy")
                             .replaceAll("(?i)zomato.*", "Zomato")
                             .replaceAll("(?i)uber.*", "Uber")
                             .replaceAll("(?i)ola.*", "Ola")
                             .replaceAll("(?i)amazon.*", "Amazon")
                             .replaceAll("(?i)flipkart.*", "Flipkart")
                             .replaceAll("(?i)dmart.*", "DMart")
                             .replaceAll("(?i)bigbasket.*", "BigBasket");
        }
        
        // If no merchant found, try to extract from common patterns
        if (merchant == null || merchant.isEmpty()) {
            if (lowMsg.contains("swiggy")) merchant = "Swiggy";
            else if (lowMsg.contains("zomato")) merchant = "Zomato";
            else if (lowMsg.contains("uber")) merchant = "Uber";
            else if (lowMsg.contains("ola")) merchant = "Ola";
            else if (lowMsg.contains("amazon")) merchant = "Amazon";
            else if (lowMsg.contains("flipkart")) merchant = "Flipkart";
            else merchant = "Other";
        }

        // Try to extract date from message; fallback to SMS timestamp
        String dateIso = null;
        try {
            java.util.regex.Matcher md1 = java.util.regex.Pattern.compile("(?:on\\s+)(\\d{1,2}\\s*[A-Za-z]{3})", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(msg);
            java.util.regex.Matcher md2 = java.util.regex.Pattern.compile("(\\d{4}-\\d{2}-\\d{2})").matcher(msg);
            if (md2.find()) {
                dateIso = md2.group(1);
            } else if (md1.find()) {
                String dmy = md1.group(1).replaceAll("\\s+", " ");
                int year = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.US).format(new Date(timestamp)));
                Date parsed = new SimpleDateFormat("d MMM yyyy", Locale.US).parse(dmy + " " + year);
                if (parsed != null) dateIso = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(parsed);
            }
        } catch (Exception ignored) {}
        if (dateIso == null) dateIso = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(timestamp));

        String low = msg.toLowerCase(Locale.US);
        String type = (low.contains("credit") || low.contains("credited") || low.contains("received")) ? "credit" : "debit";

        // Enhanced categorization with more specific rules
        String category = "others";
        
        // Food & Dining
        if (lowMsg.matches(".*(swiggy|zomato|foodpanda|dunzo|eatsure|box8|faasos|eatfit|food|restaurant|cafe|pizza|burger|domino|kfc|mcdonald|starbucks|barista|coffee|zomato|swiggy).*")) {
            category = "food";
        }
        // Travel & Transportation
        else if (lowMsg.matches(".*(uber|ola|rapido|ubermoto|uberauto|ubergo|uberxl|uberpool|rideshare|metro|train|bus|flight|airport|airlines|indigo|airasia|spicejet|vistara|makemytrip|goibibo|irctc|redbus|rapido|bike|scooter|cab|taxi|auto).*")) {
            category = "travel";
        }
        // Bills & Utilities
        else if (lowMsg.matches(".*(bill|electric|electricity|gas|water|mobile|recharge|postpaid|prepaid|dth|broadband|wifi|airtel|jio|vi|bsnl|mtnl|reliancejio|jiofiber|act|spectra|tataplay|dishtv|d2h|netflix|prime|hotstar|sonyliv|disney|spotify|youtubeprem|subscription|membership|rental|maintenance|society).*")) {
            category = "bills";
        }
        // Rent
        else if (lowMsg.matches(".*(rent|house rent|room rent|pg rent|pgrent|pg|paying guest).*")) {
            category = "rent";
        }
        // Groceries
        else if (lowMsg.matches(".*(grocery|groceries|dmart|d-mart|reliance fresh|reliance smart|spar|more|bigbasket|grofer|jiomart|bigbasket|sugarcosmetics|mamaearth|pharmeasy|netmeds|apollo|1mg|medlife).*")) {
            category = "groceries";
        }
        // Shopping
        else if (lowMsg.matches(".*(amazon|flipkart|myntra|ajio|nykaa|purplle|lenskart|titan eye|tanishq|kalyan|malabar|pc|mobile|phone|laptop|electronics|appliance|furniture|decore|home|fashion|clothing|footwear|watch|accessories|beauty|cosmetics|personal care).*")) {
            category = "shopping";
        }
        // Income
        else if (type.equals("credit") && lowMsg.matches(".*(salary|credited|refund|interest|dividend|stipend|bonus|incentive|rewards|cashback|discount).*")) {
            category = "income";
        }
        // Health
        else if (lowMsg.matches(".*(hospital|clinic|doctor|pharmacy|medicine|medicines|apollo|fortis|max|medanta|manipal|ayurveda|homeopathy|dental|eye|lasik|pharmeasy|netmeds|1mg|medlife|practo|healthkart).*")) {
            category = "health";
        }
        // Entertainment
        else if (lowMsg.matches(".*(movie|cinema|pvr|inox|imax|bookmyshow|netflix|prime|hotstar|sonyliv|zee5|voot|mxplayer|altbalaji|ullu|theatre|concert|event|ticket|bookings).*")) {
            category = "entertainment";
        }
        // Education
        else if (lowMsg.matches(".*(school|college|university|education|course|udemy|coursera|udacity|edx|byjus|unacademy|vedantu|toppr|extramarks|whitehat|coding|programming|training|workshop|seminar|webinar|certification|exam|test|entrance).*")) {
            category = "education";
        }

        // If we couldn't extract amount at all, return null so caller can skip or store as message-only
        if (amount <= 0) {
            return new TransactionModel(0, merchant, category, 0.3, dateIso, type);
        }
        return new TransactionModel(amount, merchant, category, 0.7, dateIso, type);
    }
}
