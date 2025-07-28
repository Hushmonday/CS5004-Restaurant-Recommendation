# Pure Java Restaurant Recommendation Backend

A pure Java implementation of the restaurant recommendation system with Azure OpenAI integration.

## Features

- **Pure Java**: No external dependencies, only standard Java libraries
- **HTTP Server**: Built-in Java HTTP server
- **Azure OpenAI Integration**: Direct HTTP calls to Azure OpenAI API
- **Swing UI**: Simple Java Swing graphical interface
- **REST API**: JSON-based REST endpoints

## Project Structure

```
pure-java-version/
├── src/
│   └── com/restaurant/recommendation/
│       ├── controller/     # HTTP request handling
│       ├── core/          # Main server
│       ├── model/         # Data models
│       ├── service/       # Business logic
│       └── view/          # Swing UI
├── application.properties # Configuration
├── compile.sh            # Compilation script
├── run-server.sh         # Server startup script
└── README.md            # This file
```

## Files

### Core Files (Pure Java)
- **9 Java source files**: All business logic and UI
- **1 Properties file**: Configuration (API keys, endpoints)
- **2 Shell scripts**: Compilation and execution helpers

### No External Dependencies
- ❌ No Gradle/Maven
- ❌ No Spring Boot
- ❌ No external libraries
- ❌ No XML configuration
- ✅ Only standard Java libraries

## Quick Start

1. **Compile the project**:
   ```bash
   ./compile.sh
   ```

2. **Run the server**:
   ```bash
   ./run-server.sh
   ```

3. **Run the Swing UI** (in another terminal):
   ```bash
   java -cp build com.restaurant.recommendation.view.RestaurantChatUI
   ```

## API Endpoints

- `GET /api/recommendations/health` - Health check
- `POST /api/recommendations` - Get restaurant recommendations
- `GET /api/recommendations/test-openai` - Test Azure OpenAI connection

## Configuration

Edit `application.properties` to set your Azure OpenAI credentials:

```properties
azure.openai.api.key=your-api-key
azure.openai.endpoint=https://your-resource.openai.azure.com/
azure.openai.deployment=your-deployment-name
```

## Advantages of Pure Java

1. **Simplicity**: No build tools or external dependencies
2. **Portability**: Runs anywhere with just Java installed
3. **Learning**: Great for understanding Java fundamentals
4. **Control**: Full control over every aspect of the code
5. **Size**: Minimal project footprint

## Manual Compilation

If you prefer manual compilation:

```bash
# Create build directory
mkdir -p build

# Compile all Java files
javac -d build -cp "src" src/com/restaurant/recommendation/model/*.java
javac -d build -cp "src" src/com/restaurant/recommendation/service/*.java
javac -d build -cp "src" src/com/restaurant/recommendation/controller/*.java
javac -d build -cp "src" src/com/restaurant/recommendation/core/*.java
javac -d build -cp "src" src/com/restaurant/recommendation/view/*.java

# Copy resources
cp application.properties build/

# Run
java -cp build com.restaurant.recommendation.core.RestaurantRecommendationServer
``` 