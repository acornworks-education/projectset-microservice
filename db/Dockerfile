FROM flyway/flyway
ADD src/main/db /flyway/sql

CMD ["-url=jdbc:postgresql://database:5432/postgres", "-schemas=trade", "-user=postgres", "-password=P@ssw0rd", "-connectRetries=60", "migrate"]