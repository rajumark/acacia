# Contributing to Acacia

Thank you for your interest in contributing to Acacia! This document provides guidelines and information for contributors.

## Getting Started

### Prerequisites
- Java 11 or higher
- Android SDK
- Git

### Development Setup

1. Clone the repository:
```bash
git clone https://github.com/rajumark/acacia.git
cd acacia
```

2. Build the project:
```bash
./gradlew build
```

3. Run tests:
```bash
./gradlew test
```

## Project Structure

```
acacia/
├── acacia-plugin/          # Gradle plugin implementation
│   ├── plugin/            # Plugin source code
│   └── sample/            # Sample Android app
├── docs/                  # Documentation
└── README.md
```

## How to Contribute

### Reporting Issues

- Use the GitHub issue tracker
- Provide clear reproduction steps
- Include relevant logs and environment details

### Submitting Pull Requests

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass: `./gradlew test`
6. Commit your changes: `git commit -m "Add feature"`
7. Push to your fork: `git push origin feature-name`
8. Create a pull request

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions small and focused

## Development Guidelines

### Plugin Development

- Never break builds - fail softly with warnings
- Use lazy evaluation and Provider APIs
- Declare inputs/outputs for incremental builds
- Cache heavy operations aggressively

### Testing

- Write unit tests for new functionality
- Test edge cases and error conditions
- Maintain test coverage above 80%

## Release Process

Releases are automated through GitHub Actions. Version bumps should follow semantic versioning.

## Questions?

Feel free to open an issue or start a discussion on GitHub.
