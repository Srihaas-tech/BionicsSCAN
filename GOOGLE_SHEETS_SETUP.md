# Google Sheets API Setup Instructions

To enable spreadsheet synchronization for BionicsSCAN, follow these steps:

## 1. Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Note your Project ID

## 2. Enable Google Sheets API

1. In Google Cloud Console, go to **APIs & Services** > **Library**
2. Search for "Google Sheets API"
3. Click on it and press **Enable**

## 3. Create Service Account

1. Go to **APIs & Services** > **Credentials**
2. Click **Create Credentials** > **Service Account**
3. Fill in the service account details:
   - Name: `BionicsSCAN Service Account`
   - Description: `Service account for FRC belt inventory app`
4. Click **Create and Continue**
5. Skip the roles step (click Done)
6. Click on the newly created service account
7. Go to the **Keys** tab
8. Click **Add Key** > **Create New Key**
9. Select **JSON** and click **Create**
10. Save the downloaded JSON file securely

## 4. Share Your Spreadsheet

1. Open your spreadsheet: https://docs.google.com/spreadsheets/d/1Wr_n0T0EPrmFgwfx3-Yh7oRMalMUUE5Lya4uo5zjN6A/edit
2. Click **Share** button
3. Add the service account email (from the JSON file, `client_email` field)
4. Give it **Editor** permissions
5. Click **Send**

## 5. Update Credentials File

1. Copy the contents of the downloaded JSON file
2. Replace the placeholder content in `app/src/main/assets/credentials.json`
3. Make sure the file contains your actual credentials

## 6. Spreadsheet Format

Ensure your spreadsheet has the following format:

| Column A | Column B | Column C | Column D |
|----------|----------|----------|----------|
| ID       | Length   | Quantity | Barcode  |
| 1        | 180      | 2        | BELT-180-001 |
| 2        | 225      | 5        | BELT-225-001 |
| ...      | ...      | ...      | ...      |

The app expects:
- Row 1: Headers (optional, app starts reading from row 2)
- Column A: Belt ID (unique identifier)
- Column B: Belt Length (in mm)
- Column C: Quantity (number available)
- Column D: Barcode (string identifier for scanning)

## 7. App Behavior

Once configured, the app will:
- **Sync every 1 minute**: Pull latest data from the spreadsheet
- **Update on checkout/checkin**: Immediately update both local data and spreadsheet
- **Work offline**: If credentials are missing or sync fails, app uses local data
- **Show sync status**: Display sync errors in the UI

## Troubleshooting

**"Google Sheets not configured" error:**
- Check that `credentials.json` exists in `app/src/main/assets/`
- Verify the JSON file is valid and contains all required fields

**"Sync failed" error:**
- Verify service account email has Editor access to the spreadsheet
- Check that the spreadsheet ID in `SheetsService.kt` matches your spreadsheet
- Ensure the spreadsheet has the correct format

**Permission errors:**
- Make sure the service account has the correct Google Sheets API scopes
- Verify the service account is enabled in Google Cloud Console

## Security Notes

- Never commit `credentials.json` to version control
- Keep the private key secure
- Only give the service account access to specific spreadsheets, not your entire Google Drive
