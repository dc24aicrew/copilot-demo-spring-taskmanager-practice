-- Initialize databases for microservices
CREATE DATABASE user_service_db;
CREATE DATABASE task_service_db;
CREATE DATABASE notification_service_db;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE user_service_db TO taskmanager;
GRANT ALL PRIVILEGES ON DATABASE task_service_db TO taskmanager;
GRANT ALL PRIVILEGES ON DATABASE notification_service_db TO taskmanager;