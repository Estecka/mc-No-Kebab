# v1
## 1.0
Initial Release

## 1.1
### 1.1.0
- **No-Kebab is now fully optional on clients**.
- Removed the `nokebab:missingno` variant.
- Clients without the mod will still see `minecraft:kebab` as missing paintings.
- Clients with the mod installed will be able to see the missing painting's ID
### 1.1.
- Missing ID label is now rendered with a full outline instead of a shadow.

## 1.2
### 1.2.0
- Added the command `/nokebab migrate`
- Fixed some benign warnings when running on server-only environnement.
### 1.2.1
- Marked as incompatible with MC 1.20.5

## 1.3
### 1.3.0
- Ported to MC 1.20.5.
- Added the command `/nokebab give`.
- Custom DataTrackers can be disabled in the config. Those are off by default on dedicated servers.
### 1.3.1
- Updated for MC 1.21.
