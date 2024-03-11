# project-final JavaRush
***
### JiraRush
Финальный проект JavaRush University

#### Запуск
```
docker run -p 5432:5432 --name postgres-db -e POSTGRES_USER=jira -e POSTGRES_PASSWORD=JiraRush -e POSTGRES_DB=jira -e PGDATA=/var/lib/postgresql/data/pgdata -v ./pgdata:/var/lib/postgresql/data -d postgres
docker run -p 5433:5432 --name postgres-db-test -e POSTGRES_USER=jira -e POSTGRES_PASSWORD=JiraRush -e POSTGRES_DB=jira-test -e PGDATA=/var/lib/postgresql/data/pgdata -v ./pgdata-test:/var/lib/postgresql/data -d postgres
```
```
mvn clean install -DskipTests
docker build -t javarush-jira .
docker run -p 8080:8080 --name javarush-jira -d javarush-jira
```
http://localhost:8080/

http://localhost:8080/swagger-ui/index.html

Учетная запись администратора JiraRush:

admin@gmail.com  
admin

### Список выполненных задач:

1. [x] (1) Разобраться со структурой проекта (onboarding).
2. [x] (2) Удалить социальные сети: vk, yandex.
3. [x] (3) Вынести чувствительную информацию в отдельный property файл: логин, пароль БД, идентификаторы для OAuth регистрации/авторизации, настройки почты
4. [x] (4) Переделать тесты так, чтоб во время тестов использовалась in memory БД (H2), а не PostgreSQL. Для этого нужно определить 2 бина, и выборка какой из них использовать должно определяться активным профилем Spring. H2 не поддерживает все фичи, которые есть у PostgreSQL, поэтому тебе прийдется немного упростить скрипты с тестовыми данными.
5. [x] (5) Написать тесты для всех публичных методов контроллера ProfileRestController. Хоть методов только 2, но тестовых методов должно быть больше, т.к. нужно проверить success and unsuccess path.
6. [x] (6) Сделать рефакторинг метода com.javarush.jira.bugtracking.attachment.FileUtil#upload чтоб он использовал современный подход для работы с файловой системой
7. [ ] (7) Добавить новый функционал: добавления тегов к задаче (REST API + реализация на сервисе). Фронт делать необязательно. Таблица task_tag уже создана.
8. [ ] (8) Добавить подсчет времени сколько задача находилась в работе и тестировании. Написать 2 метода на уровне сервиса, которые параметром принимают задачу и возвращают затраченное время:  
   **•** Сколько задача находилась в работе (ready_for_review минус in_progress).  
   **•** Сколько задача находилась на тестировании (done минус ready_for_review).  
   Для написания этого задания, нужно добавить в конец скрипта инициализации базы данных changelog.sql 3 записи в таблицу ACTIVITY  
   ```sql
   insert into ACTIVITY (ID, AUTHOR_ID, TASK_ID, UPDATED, STATUS_CODE) values (...);
   ```
   Со статусами:  
   **•** время начала работы над задачей – in_progress  
   **•** время окончания разработки - ready_for_review  
   **•** время конца тестирования - done  
9. [x] (9) Написать Dockerfile для основного сервера
10. [ ] (10) Написать docker-compose файл для запуска контейнера сервера вместе с БД и nginx. Для nginx используй конфиг-файл config/nginx.conf. При необходимости файл конфига можно редактировать. Hard task
11. [ ] (11) Добавить локализацию минимум на двух языках для шаблонов писем (mails) и стартовой страницы index.html.
12. [ ] (12) Переделать механизм распознавания «свой-чужой» между фронтом и беком с JSESSIONID на JWT. Из сложностей – тебе придётся переделать отправку форм с фронта, чтоб добавлять хедер аутентификации. Extra-hard task
