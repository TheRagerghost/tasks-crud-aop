# Задача 1. Tasks CRUD + AOP
Небольшое приложение, реализующее RESTful сервис с логированием через аспекты. Для обучения.

### БД

```bash
docker run --name tasksapp-pgdb -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=tasksappdb -p 5432:5432 -d postgres
```

### Чеклист

+ Task (id, title, description, userId)
+ GET /tasks/{id}
+ GET /tasks
+ POST /tasks
+ PUT /tasks/{id}
+ DELETE /tasks/{id}
+ Аспект TaskAspect для логирования
    - `@Around` для замера времени выполнения
    - `@Before` для ограничения выполнения метода для открытого API
    - `@AfterReturning` для стилизации разных типов задач в логе
    - `@AfterThrowing` для дополнительной информации об ошибке