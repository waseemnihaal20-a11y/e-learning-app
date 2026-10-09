# Learning Dashboard

A small Android app that shows a learner's courses and lets them mark lessons as completed.
It was built as a time-boxed technical assignment, so the focus is architecture, state
handling, offline support, error handling and tests, not UI polish.

**Application walkthrough demo:** https://www.loom.com/share/9a11da1b09624d5eb47495f7f7fdaea4

## 1. Overview

Three screens:

1. **Login**: email and password fields with validation, a loading state and an error message. Authentication is mocked.
2. **Course Dashboard**: a list of courses with title, instructor, progress %, a progress bar, lesson count and a Continue button. It handles Loading, Success, Empty and Error states.
3. **Course Details**: the course title, current progress and the list of lessons. Each lesson is shown as completed or pending, and a pending lesson can be marked completed.

Stack: Kotlin, Jetpack Compose (Material 3), MVVM with a Repository, ViewModel + StateFlow,
Room (with KSP), Kotlin Coroutines and Flow, Navigation Compose, and manual constructor
dependency injection (no Hilt). Package: `com.intellipaat.course`. Min SDK 24, target SDK 37.

## 2. How to run

Requirements:

- Android Studio 2026.2 (the version used to build this project) or newer.
- The Gradle daemon uses JDK 25, as set in `gradle/gradle-daemon-jvm.properties`. Gradle downloads it automatically if it is missing.
- To run `./gradlew` from a terminal you need JDK 17 or newer on your PATH (Gradle 9.6 itself needs at least 17).

Steps:

1. In Android Studio, choose **File > Open** and select the project folder. Let Gradle sync finish.
2. Pick an emulator or a connected device (USB debugging on) in the device dropdown.
3. Click **Run**.

From a terminal:

```bash
./gradlew :app:installDebug        # build and install on the connected device or emulator
./gradlew :app:testDebugUnitTest   # run the unit tests (JVM only, no device needed)
```

## 3. Demo credentials

- Any valid email (for example `user@example.com`) with a password of 6 or more characters logs in.
- The password `wrongpass` always fails, so the login error state can be shown.
- Login takes about 1 second (a fake delay) so the loading state is visible.

## 4. Architecture

```
ui       Compose screens  ->  ViewModels (StateFlow of a sealed UI state)
              |
domain   Course, Lesson, calculateProgress()       (plain Kotlin, no Android)
              |
data     CourseRepository  ->  Room (CourseDao)     <- single source of truth
                           ->  FakeCourseApi        (only writes into Room)
```

Package layout (`app/src/main/java/com/intellipaat/course/`):

```
AppContainer.kt, LearningDashboardApp.kt, MainActivity.kt
data/local        CourseEntity, LessonEntity, CourseWithLessons, CourseDao, AppDatabase, Mappers
data/remote       FakeCourseApi, CourseDto/LessonDto, ConnectivityChecker
data/repository   CourseRepository (interface) + CourseRepositoryImpl, AuthRepository + FakeAuthRepository
domain/model      Course, Lesson, calculateProgress
ui                AppNavHost, AppViewModelProvider
ui/login, ui/dashboard, ui/details   one Screen, ViewModel and UiState file per screen
```

Why MVVM + Repository: screens only render state and send events, so they stay simple.
ViewModels hold screen logic and survive rotation. The repository hides where data comes
from, so ViewModels can be unit tested with a fake repository. `AppContainer` builds every
long-lived object once (manual DI), and `AppViewModelProvider` builds ViewModels from it.

Key rules:

- **Room is the single source of truth.** The UI only observes Room through `Flow`.
  `FakeCourseApi` never feeds the UI directly. `CourseRepositoryImpl.refresh()` fetches
  courses and writes them into Room in one `@Transaction` (`CourseDao.saveCourses`). Room
  then emits the new data to every screen that is observing it.
- **Progress is never stored.** There is no progress column. `calculateProgress(lessons)` in
  `domain/model/Course.kt` returns `completed * 100 / total`, and 0 when there are no lessons.
  Integer division means a course only shows 100% when every lesson is done.
- **A refresh never undoes the user's progress.** Lesson IDs are deterministic
  (`courseId * 1000 + index`). Lessons are inserted with `OnConflictStrategy.IGNORE`, so an
  existing lesson, including its completed flag, is never overwritten. Courses use `@Upsert`
  instead of `REPLACE`, because `REPLACE` deletes the row first and the foreign key's
  `CASCADE` would delete that course's lessons.
- **Marking a lesson completed** only updates Room. The dashboard and details screens update
  on their own because their Room queries observe the `lessons` table.

## 5. Offline support

What is cached: all courses and lessons, including which lessons the user has completed, in
the Room database `learning_dashboard.db`.

When a fetch happens: the dashboard's ViewModel starts one refresh when it is created (after
login), and again when the user taps Retry. There is no pull-to-refresh or background sync.

Connectivity: `ConnectivityChecker` asks Android's `ConnectivityManager` whether the active
network has internet. `FakeCourseApi` waits 1 second, then throws
`IOException("No internet connection")` if the device is offline. Airplane mode really makes
the fetch fail.

How the dashboard decides what to show (`DashboardViewModel`):

| Room has courses? | Fetch status | Screen shows |
|---|---|---|
| Yes | any | The course list. If the fetch failed, a Snackbar shows the message once. |
| No | running | Loading spinner |
| No | failed | Error screen with the message and a Retry button |
| No | succeeded with 0 courses | Empty message |

So the Error screen only appears when there is nothing cached. With a cache, a failed refresh
keeps the list on screen and only shows a non-blocking Snackbar. Retry clears any old message
before it starts.

## 6. Security

This app mocks authentication and stores nothing: no token, no password and no session.
`FakeAuthRepository` only waits and returns success or failure.

In production I would:

- Store the auth token in DataStore, encrypted with a key kept in the Android Keystore. Never
  in plain SharedPreferences, and never written to logs.
- Use HTTPS only, with no cleartext traffic allowed.
- Use short-lived access tokens with a refresh token, refreshing them automatically when the
  API returns 401.
- On logout, delete the tokens and clear the local database.

## 7. Scaling to 1M users

- **Real backend and sync**: replace `FakeCourseApi` with a real API. Use delta sync (only
  courses changed since a stored `updatedAt`, or ETag / `If-None-Match`) so refreshes stay cheap.
- **Pagination**: Paging 3 with a `RemoteMediator`, so Room stays the single source of truth for large catalogues.
- **Background sync**: WorkManager to refresh periodically and to upload completed lessons when the network is back.
- **Server-side progress**: send lesson completions to the server and handle conflicts
  between devices (for example, "completed" always wins, since a lesson is never un-completed).
- **Images**: course thumbnails from a CDN with an image cache (for example Coil).
- **Database migrations**: export the Room schema and write tested migrations instead of bumping the version blindly.
- **As the team grows**: Hilt for dependency injection and feature modules to keep build times and ownership clear.
- **Monitoring**: crash reporting and analytics (for example Firebase Crashlytics and Analytics).

## 8. Porting to iOS

| Android (this app) | iOS |
|---|---|
| Jetpack Compose | SwiftUI |
| ViewModel + StateFlow | `@Observable` class (or `ObservableObject` with `@Published`) |
| Room | SwiftData or Core Data |
| Navigation Compose | `NavigationStack` |
| Coroutines and Flow | async/await and `AsyncSequence` |
| `ConnectivityManager` | `NWPathMonitor` |

What stays the same: the layering (UI, ViewModel, repository, local store as the single
source of truth), the repository interface, the sealed UI states (Swift enums with associated
values), and the `calculateProgress` rule. Kotlin Multiplatform could also share the domain
and data layers between both apps.

## 9. Testing

`./gradlew :app:testDebugUnitTest` runs 19 JVM unit tests (18 for the app, plus the template
`ExampleUnitTest`).

- `CourseRepositoryImplTest` (5): refresh saves everything in one transaction and returns
  the course count; a refresh keeps a completed lesson; refreshing twice creates no
  duplicates; a failed refresh leaves cached data untouched; a failed refresh on an empty
  database saves nothing.
- `DashboardViewModelTest` (6): the first load shows Loading and never Empty; Empty only
  appears after a successful fetch of zero courses; a failure without a cache shows Error; a
  failure with a cache shows Success with a message; `onMessageShown` clears only the message;
  Retry clears the old message.
- `LoginViewModelTest` (5): no errors before tapping Login; an invalid email is rejected; a
  short password is rejected; valid input shows loading and then logs in; `wrongpass` shows the error.
- `CourseDetailsViewModelTest` (2): an unknown course shows NotFound; marking a lesson
  completed updates its status and the progress.

The repository tests use `FakeCourseDao`, an in-memory copy of the DAO that imitates Room's
upsert and IGNORE rules. They do not run Room's real SQL. The next step is an instrumented
test in `androidTest` using `Room.inMemoryDatabaseBuilder` to check the real queries.

## 10. Known trade-offs and next steps

- **Login is not remembered.** Every app launch starts at the Login screen.
- **Strings are hard-coded** in the screens and ViewModels. Next: move them to string resources.
- **Lessons can be completed in any order.** Next: sequential locking, if the product wants it.
- **Brief Loading on the first frame.** The dashboard state starts as Loading until Room's first emission, even when data is cached.
- **No accessibility or dark-theme polish** beyond what the Material 3 template provides.
- **The launcher icon is still the template icon.** The IntelliPat logo has not been added yet.
- **Lesson write failures are not caught.** If the Room write in `markLessonCompleted` throws, the app crashes. Next: catch it and show a message.
- **No way to undo a completed lesson**, and the fake API's changes to existing lessons are ignored by design.
