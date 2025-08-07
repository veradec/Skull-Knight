<p align="center">
  <img src="app-icon.jpg" alt="SkullKnight Icon" width="120"/>
  <h1 align="center">SkullKnight: A TodoList with a Daemon</h1>
</p>

# Skull Knight

This Android app provides a modern mobile interface for the project management system that works with the `cli.sh` script. It allows you to manage projects and items with different statuses through a beautiful Material Design 3 interface.

## Features

- **Project Management**: Create, view, and delete projects
- **Item Management**: Add items to projects with optional parent-child relationships
- **Status Tracking**: Update item statuses (Not Initiated, In Progress, Completed, Near Complete)
- **Reason Tracking**: Add reasons for status changes
- **Hierarchical Display**: View items in a tree structure with proper indentation
- **Color-coded Status**: Visual status indicators with appropriate colors
- **Modern UI**: Built with Jetpack Compose and Material Design 3

## Prerequisites

1. **Backend Server**: The app requires the backend server to be running on `http://127.0.0.1:8000`

2. **Android Development Environment**:
   - Android Studio
   - Android SDK (API level 24+)
   - Kotlin

## Setup

1. **Start the Backend Server**: Make sure your backend server is running on `http://127.0.0.1:8000`

2. **Build and Run the App**:
   ```bash
   # In the project directory
   ./gradlew assembleDebug
   # Or use Android Studio to build and run
   ```

3. **Network Configuration**: The app is configured to allow cleartext traffic for localhost development. For production, you should use HTTPS.

## Usage

### Project List Screen
- View all your projects
- Tap the + button to create a new project
- Tap on a project to view its details
- Swipe or use the delete button to remove projects

### Project Detail Screen
- View all items in the selected project
- Tap the + button to add new items
- Use the edit button on items to update their status
- Use the delete button to remove items
- Items can have parent-child relationships for hierarchical organization

### Status Management
- **Not Initiated** (Grey): Default status for new items
- **In Progress** (Green): Item is currently being worked on
- **Completed** (Orange): Item has been finished
- **Near Complete** (Light Blue): Item is almost finished

## Architecture

The app follows MVVM (Model-View-ViewModel) architecture with the following components:

- **Data Layer**: API service, repository, and data models
- **UI Layer**: Jetpack Compose screens and components
- **ViewModel Layer**: State management and business logic
- **Navigation**: Single Activity with Compose Navigation

## License

This project is part of the Skull Knight's Checklist system and is Licensed under MIT LICENSE
