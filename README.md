# Задача 1. Tasks CRUD + AOP
Небольшое приложение, реализующее RESTful сервис с логированием через аспекты. Для обучения.

### БД

```bash
docker run --name tasksapp-pgdb -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=tasksappdb -p 5432:5432 -d postgres
```