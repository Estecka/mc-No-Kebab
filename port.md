# Minecraft Code Breaking Changes
### 1.19.4
Current master

### 1.20
#### No Workaround:
- `ServerCommandSource::sendFeedback` takes a Text supplier instead of a Text.

### 1.20.5
#### No Workaround:
- `DataTracker::startTracking` was removed. `Entity::initDataTracker` now takes a builder as parameter.
#### Possible Workaround
- `PaintingEntity::VARIANT_NBT_KEY` was removed: Use literal instead.
