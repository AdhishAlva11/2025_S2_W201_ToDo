# 🌍 To-Do Travel App  

An Android application designed to help users **discover, organize, and track travel activities** with features like Google Maps integration, distance calculation, multilingual support, and activity filtering.  
Developed as part of the **COMP602 – Software Development Practice** paper at Auckland University of Technology (AUT), following the **Scrum methodology**.  

---
**Developers**

- Adhish Alva – Developer
- Phoenix Gordon-Stables – Developer
- Stephan Teaca-Jucan – Scrum Master
- Ziraak Wadia – Product Owner

---

## Features  

**Core Functionality**

- Search nearby activities using the Google Places API.

- Display locations on Google Maps and show distance from the user’s current position.

- Save favourite activities to a personalized itinerary.

**Advanced Features**

- Multi-language support: English, Hindi, and Chinese, with dynamic switching.

- Activity pricing: Displays price levels from Google Places (if available).

- Distance display: Shows distance between the search location and selected activity.

- Dark / Light mode: User’s preference is automatically saved across sessions.
  
- Mark activities as completed using an interactive checkbox system.

**User Management**

- Firebase Authentication for email/password and Google Sign-In.
- Cloud Firestore + Realtime Database for saving itineraries, favourites, and user settings.
- Firebase Storage for user profile photos.

**Emergency Contact Feature**

- Automatically detects the user’s current country and displays the local emergency number (e.g. 111 in NZ, 000 in Australia, 911 in the US).

- The number is clickable and opens the dialer instantly.

---
## **User Flow**

Login → Search Activities → View on Map → Add to Favourites → Create Itinerary → Upload Photo → View Emergency Contact → Change Language / Theme

---
## **How the App Works**

1. **User Login / Registration**
  - Firebase Authentication handles email/password and Google Sign-In.
2. **Explore Activities**
  - Loads places near the user via Google Places API and maps them visually.
3. **Save Activities**
  - Users can add places to Favourites or Itineraries stored in Firebase.
4. **Personalize Profile**
  - Upload profile photo, switch language or theme, and see emergency contact for current country.
5. **Manage Itinerary**
  - Create multi-day plans and mark each activity as completed.

---
## How to Use
1. Launch the app and **sign in** using Email or Google.  
2. **Search** for a country or city to explore nearby activities.  
3. Tap any **map marker** to view details or save it to your **Favourites** or **Itinerary**.  
4. Open the **Itinerary** tab to view your saved plans and mark activities as completed.  
5. Visit the **Profile** page to upload a profile photo and view your local emergency contact.  
6. Customize your experience by changing the **theme** or **language** in the **Settings** screen.  
---

## Tech Stack  

| Category               | Technologies Used                                  |
| ---------------------- | -------------------------------------------------- |
| **Languages**          | Kotlin, Java                                       |
| **IDE**                | Android Studio                                     |
| **Backend / Database** | Firebase (Auth, Firestore, Storage, Realtime DB)   |
| **API Integration**    | Google Maps SDK, Google Places API, Directions API |
| **UI / UX**            | XML Layouts, Material Design, RecyclerView, Glide  |
| **Architecture**       | MVVM-style with Firebase backend                   |
| **Version Control**    | Git + GitHub                                       |
| **Methodology**        | Scrum (Trello board + Burndown charts)             |

---

## Setup & Installation  

### Prerequisites  
- Android Studio (latest version)  
- Google Maps API key  
- Firebase project (with Authentication and Firestore enabled)

### Steps  
1. **Clone the repository.**  

2. **Open in Android Studio**
   Go to File → Open → select the cloned folder.
3. **Sync Gradle**
   Android Studio automatically downloads all dependencies.
4. **Run the App**
   Connect a device or start an emulator → click -> Run.
**No extra setup required.**
– Google Maps API Key is already configured in res/values/strings.xml.
– Firebase services (Auth, Firestore, Storage) are linked via google-services.json.
– The app is ready to run immediately after cloning.
---
## API Integration Overview
| API                               | Purpose                                |
| --------------------------------- | -------------------------------------- |
| **Google Maps SDK**               | Displays interactive maps and markers. |
| **Google Places API**             | Fetches nearby activities and details. |
| **Google Directions API**         | Calculates distance and travel time.   |
| **Firebase Authentication**       | Manages user sign-in and Google Auth.  |
| **Firebase Database / Firestore** | Stores itineraries and favourites.     |
| **Firebase Storage**              | Saves profile images.                  |

---
## Version Info
- **Version:** 1.0.0  
- **Minimum SDK:** 29 (Android 10)  
- **Target SDK:** 34 (Android 14)  
- **Compatible Devices:** Android phones & tablets  
---
## Scrum & Project Management  

This project was developed using **Scrum** practices:  
- **Sprints:** Each sprint focused on incremental feature delivery (e.g., Sprint 1 – Maps setup, Sprint 2 – Activity search & filters).  
- **Daily Stand-ups:** Short team meetings to discuss progress, blockers, and upcoming tasks.  
- **Burndown Chart:** Used to track story point progress and sprint performance.  
- **Definition of Done (DoD):** Each feature was considered complete only after being tested, reviewed, and merged into the main branch.  
---


