# Budget Tracker – Android App

An Android budget planning and spending tracker built in Kotlin, designed to help students and young adults who are managing their money on their own for the first time. Users set a weekly or monthly budget, record expenses by category, and see where their money is going through charts, comparisons and spending alerts.

> Built as an individual project for the Mobile Development module at Cardiff University.

## Features

- Weekly or monthly budgets with a live remaining-balance indicator
- Add, edit and delete expenses with amount, category and date
- Built-in categories (food, rent, transport, entertainment) plus your own custom categories
- Recurring expenses, deducted automatically on their due date
- Spending summary for the current period, a pie chart by category, and comparisons with the previous period
- Notifications at 70%, 90% and 100% of the budget, and when you go over it
- Personalised spending tips
- All data stored locally on the device

## Tech stack

Kotlin · XML layouts (ConstraintLayout, RecyclerView) · Room · ViewModel / LiveData · DataStore Preferences · WorkManager · NotificationCompat

## Getting started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (a recent version)
- JDK 17 (bundled with Android Studio)
- An emulator or Android phone running Android 10 (API 29) or newer

### Run in Android Studio

1. Clone the repository: `git clone <this-repo-url>`
2. In Android Studio choose **File → Open** and select the **`4-application`** folder
3. Wait for the Gradle sync to finish
4. Select an emulator or connected device and press **Run ▶**

### Build from the command line

```bash
cd 4-application
./gradlew assembleDebug        # on Windows: gradlew.bat assembleDebug
```

The APK is written to `4-application/app/build/outputs/apk/debug/`.
