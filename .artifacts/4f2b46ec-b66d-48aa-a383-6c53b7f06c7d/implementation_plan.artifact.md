# New Design for Wellness Score & Daily Checklist

This plan outlines the redesign of the Wellness Score card and Daily Checklist section in the `HomeActivity` to provide a more modern, engaging, and informative user interface.

## User Review Required

> [!NOTE]
> The new design uses Material 3 components and custom drawing for a more premium look. The colors will automatically adapt to the application's theme.

## Proposed Changes

### [Component Name]

#### [MODIFY] [HomeActivity.kt](file:///Users/saidurrahman/AndroidStudioProjects/Helps/app/src/main/java/com/srcreation/smarthelp/HomeActivity.kt)

1.  **Redesign `WellnessScoreCard`**:
    *   Implement a glassmorphism-inspired card with a subtle gradient background.
    *   Enhance the circular progress indicator with a more prominent size and modern styling.
    *   Add a secondary "Level" indicator (e.g., Bronze, Silver, Gold based on score).
    *   Include a brief summary of the score's meaning.

2.  **Redesign `DailyChecklistSection`**:
    *   Improve the task item layout to be more vertical-friendly or a more refined horizontal scroll.
    *   Use cards for each task with distinct icons and clear completion states.
    *   Add a progress bar for the overall checklist completion.
    *   Implement better visual feedback when a task is completed (e.g., color shift, icon change).

## Verification Plan

### Automated Tests
- I will run the build to ensure there are no compilation errors.
- I will use `render_compose_preview` (if applicable) or rely on manual verification via screenshots.

### Manual Verification
- Deploy the app to a device/emulator.
- Observe the new Wellness Score card and verify the circular indicator reflects the score accurately.
- Interact with the Daily Checklist and ensure completion states are visually distinct and the progress indicator updates correctly.
