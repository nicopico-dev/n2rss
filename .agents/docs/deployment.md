# Deployment

## Deploying the Application

The project includes a custom deployment task:

```bash
./gradlew copyJarToDeploy
```

This creates a JAR file named `n2rss.jar` in the `deploy` directory.

## Continuous Integration

The project includes test scripts for CI/CD:

- `test_ci.sh`: CI test script
- `test_cd.sh`: CD test script
- `test_restart_server.sh`: Server restart test script