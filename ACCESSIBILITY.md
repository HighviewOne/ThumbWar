# Accessibility Features

ThumbWar is designed to be accessible to all users, including those with disabilities. This document outlines our accessibility features and guidelines.

## Implemented Features

### 1. Content Descriptions

All interactive and meaningful UI elements include content descriptions (`.contentDescription()`) for screen reader users:

```kotlin
Button(
    onClick = { ... },
    modifier = Modifier.semantics {
        contentDescription = "Start new game against computer"
    }
) {
    Text("VS Computer")
}
```

**Added descriptions include:**
- Button labels and purposes
- Screen titles and purposes
- Visual indicators (colors, animations)
- Icon meanings

### 2. Semantic Labels

Jetpack Compose components are properly labeled with semantic information for accessibility tools:

```kotlin
Modifier.semantics {
    contentDescription = "Player 1 thumb position"
}
```

### 3. Touch Target Sizes

All interactive elements meet minimum accessibility standards:
- **Minimum size**: 48dp × 48dp (Material Design recommendation)
- **Thumbs in game**: 40dp radius = 80dp × 80dp (exceeds minimum)
- **Buttons**: 56dp minimum height with adequate padding

### 4. Color Contrast

Visual elements use sufficient color contrast:
- **Text on background**: WCAG AA compliant (4.5:1 ratio minimum)
- **UI elements**: High contrast colors for visibility
- **Game elements**: Distinct colors for player 1 and player 2 thumbs

### 5. Screen Reader Support

Compatible with Android's TalkBack screen reader:
- Logical reading order (top to bottom, left to right)
- Meaningful element grouping
- Clear component descriptions
- Proper semantic hierarchy

### 6. Haptic Feedback

Vibration feedback for important events (togglable in settings):
- Game start/end
- Successful pin
- Round/match transitions
- UI interactions

## Game Accessibility

### Audio Cues

Sound effects for game events (togglable in settings):
- Countdown warnings
- Pin progress feedback
- Round/match results
- UI interactions

### Visual Indicators

Besides color, game states use multiple indicators:
- **Pin progress ring**: Animated visual feedback
- **Score text**: Clear numerical display
- **Phase text**: Explicit game phase announcements
- **Touch feedback**: Visual response to player input

### Settings

Players can customize accessibility features:
- Enable/disable sound effects
- Enable/disable vibration feedback
- Audio description announcements (future)

## Screen-by-Screen Accessibility

### Main Menu Screen
- ✅ Title clearly identifies the app
- ✅ Buttons have descriptive labels
- ✅ Current win streak displayed
- ✅ Settings button accessible

### Difficulty Selection Dialog
- ✅ Clear descriptions of each difficulty level
- ✅ Easy/Medium/Hard with explanations
- ✅ Help button for additional information

### Game Screen
- ✅ Current score readable
- ✅ Game phase announced (Countdown, Playing, Pin In Progress)
- ✅ Pin progress ring has text alternative
- ✅ Touch areas properly sized

### Game Over Screen
- ✅ Winner clearly announced
- ✅ Final score displayed
- ✅ Statistics readable
- ✅ Next action buttons clear

### Settings Screen
- ✅ Toggle switches have labels
- ✅ Current settings visible
- ✅ Help text for each setting

## Testing Accessibility

### Using TalkBack (Screen Reader)

On Android device:
1. Settings → Accessibility → TalkBack
2. Turn TalkBack on
3. Navigate app with screen reader:
   - Single finger swipe right: Next element
   - Single finger swipe left: Previous element
   - Double tap: Activate element
   - Swipe up then down: Open reading controls

**Expected behavior:**
- All buttons, text, and UI elements are announced
- Reading order is logical and consistent
- Element purposes are clear and descriptive

### Using Accessibility Checker

In Android Studio:
1. Tools → Accessibility Tools → Accessibility Checker
2. Run checks on layouts
3. Fix any accessibility warnings

### Manual Testing

Checklist:
- [ ] All buttons/interactive elements respond to touch
- [ ] Button purposes are clear
- [ ] Text is readable against background
- [ ] Colors alone don't convey essential information
- [ ] Touch targets are at least 48dp
- [ ] Keyboard navigation works (if applicable)
- [ ] TalkBack announces all meaningful elements

## Code Guidelines

### Adding Accessible Components

Always include content descriptions:

```kotlin
// ✅ Good
Button(
    onClick = { ... },
    modifier = Modifier.semantics {
        contentDescription = "Start single player game"
    }
) {
    Text("Play")
}

// ❌ Bad
Button(onClick = { ... }) {
    Icon(...)  // No description
}

// ❌ Bad
Button(onClick = { ... }) {
    Text("A")  // Unclear label
}
```

### Text Accessibility

```kotlin
// ✅ Good - Clear, descriptive
Text("Player 1 wins: 3")

// ❌ Bad - Ambiguous
Text("P1: 3")

// ❌ Bad - Color-only meaning
Text(
    "Score",
    color = if (isWinning) Green else Red
)
```

### Semantic Modifiers

```kotlin
// ✅ Good - Proper semantics
Canvas(modifier = Modifier
    .fillMaxSize()
    .semantics {
        contentDescription = "Game arena with two thumbs"
    }
)

// ❌ Bad - No semantics
Canvas(modifier = Modifier.fillMaxSize())
```

## Accessibility Roadmap

Planned improvements:

1. **Audio Descriptions**: Narrated game instructions (future)
2. **High Contrast Mode**: Dark mode with increased contrast (future)
3. **Text Size Options**: User-controllable text scaling (future)
4. **Customizable Colors**: Theme options for color-blind users (future)
5. **Keyboard Navigation**: Full keyboard control support (future)
6. **Device Shake Detection**: Shake to perform actions (future)

## Resources

- [Android Accessibility Overview](https://developer.android.com/guide/topics/ui/accessibility)
- [Material Design Accessibility](https://material.io/design/usability/accessibility.html)
- [Jetpack Compose Accessibility](https://developer.android.com/develop/ui/compose/accessibility)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Android Accessibility Test Framework](https://github.com/google/Accessibility-Test-Framework-for-Android)

## Feedback

If you encounter accessibility issues:
1. Open a GitHub issue with:
   - Description of the problem
   - Device and Android version
   - Steps to reproduce
   - Screenshot if applicable

2. Contact us directly with suggestions

We appreciate all feedback to make ThumbWar more accessible for everyone!
