# Accessibility

What ThumbWar does for accessibility today, where it falls short, and how to add to it.

## What Works

- **Menus with a screen reader.** They're built from standard Material 3 buttons, chips and
  text, so TalkBack reads their labels and can activate them. No custom descriptions have been
  added.
- **Touch targets.** Menu buttons are 48–56 dp tall; dialog, chip and switch targets use the
  Material 3 defaults.
- **Game events aren't color-only.** The score, "Player N pinning!", "Player N wins the
  round!" and the countdown are shown as text, and events also have sounds and vibration
  (each switchable in Settings).
- **Landscape and large text.** The menu, settings and game-over screens scroll when their
  content doesn't fit.

## Known Gaps

- **Settings switches are unlabeled for screen readers.** "Sound Effects" and "Vibration" are
  separate text beside each switch, so TalkBack announces just "Switch, on". Making each row
  one toggleable element (`Modifier.toggleable` on the row) would fix it.
- **Gameplay isn't usable with a screen reader.** The arena is drawn on a Canvas with no
  semantics, so TalkBack can't describe where the thumbs are, and playing requires dragging a
  finger precisely.
- **Players are told apart by color.** P1 and P2 thumbs differ only in skin tone and outline
  color.
- **Contrast hasn't been measured** against WCAG ratios.
- **Countdown and results aren't announced** to screen readers as they change.

## Roadmap

1. **Labeled settings switches**: merge each label and switch into one toggleable row
2. **Arena semantics**: describe the thumbs' positions and the pin state (live region)
3. **Announcements**: countdown, pin start/escape and round results as screen reader announcements
4. **Distinct player markers**: a shape or label on each thumb, not just color
5. **Contrast check**: measure text and UI contrast against WCAG AA and adjust
6. **Audio Descriptions**: narrated game instructions
7. **Customizable Colors**: theme options for color-blind users
8. **Keyboard Navigation**: full keyboard control support
9. **Device Shake Detection**: shake to perform actions

## Testing Accessibility

### With TalkBack

1. Settings → Accessibility → TalkBack → on
2. Swipe right/left to move between elements, double-tap to activate
3. Check that every menu and settings control is announced with a clear label and state

### Checklist

- [ ] All buttons and controls are announced with a clear purpose
- [ ] Text is readable against its background
- [ ] Color alone doesn't carry essential information
- [ ] Touch targets are at least 48 dp
- [ ] Screens are usable in landscape and with the largest font size

## Code Guidelines

Standard Material components are labeled by their text. Add descriptions where there's no
text, or where the text isn't enough on its own:

```kotlin
// Icon-only button: needs a description
IconButton(onClick = onBack) {
    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
}

// Custom drawing: describe what it shows
Canvas(
    modifier = Modifier
        .fillMaxSize()
        .semantics { contentDescription = "Arena: player 1 left, player 2 right" }
) { /* ... */ }
```

Prefer text that stands on its own ("Player 1 pinning!") over meaning carried only by color.

## Resources

- [Android Accessibility Overview](https://developer.android.com/guide/topics/ui/accessibility)
- [Jetpack Compose Accessibility](https://developer.android.com/develop/ui/compose/accessibility)
- [Material Design Accessibility](https://m3.material.io/foundations/overview)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Android Accessibility Test Framework](https://github.com/google/Accessibility-Test-Framework-for-Android)

## Feedback

If you run into an accessibility problem, please open a GitHub issue with a description, your
device and Android version, and steps to reproduce.
