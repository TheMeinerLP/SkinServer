# Changelog

## [Unreleased]
### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [1.0.3] - 2021-11-08
### Added
- Set spring application name to `skinserver` for kubernetes
- Disable for swagger generation k8s support
- Bump dependencies
### Changed

### Deprecated

### Removed

### Fixed

### Security

## [1.0.2] - 2021-11-08
### Changed
- **Every url must be ended with `/`**
- Improve code readability

### Fixed
- Rotation is now optional and as a path parameter
    - Values:
        - Front
        - Back
        - Right
        - Left
        - Top
        - Bottom

## [1.0.1] - 2021-11-07
### Added
- Added kubernetes support
- Add `liveness` and `readiness` endpoints

## [1.0.0] - 2021-11-07
### Added
- Improved code quality
- Basic swagger docs
- New code structure
- Better gradle config
- Improve dockerized deployment
- [Automatic Swagger docs](https://themeinerlp.github.io/SkinServer/)