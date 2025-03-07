## Introduction
This project is an example of RabbitMQ producing/consuming with Spring Boot.

The repository contains two Spring Boot projects :
- a RabbitMQ producer fed by a controller (REST API)
- a RabbitMQ consumer that displays the received records in the logs

## Run the project
### RabbitMQ environment
To deploy start RabbitMQ you will need docker installed and run the `docker/docker-compose.yml` file.
```
docker-compose -f docker/docker-compose.yml up -d
```

### Application
Once RabbitMQ has started and is healthy, you can start the Spring Boot projects and try them out.

Save a movie
```
curl --request POST \
  --url http://localhost:8090/rabbitmq-producer/movies \
  --header 'Content-Type: application/json' \
  --data '{
	"id": 26,
	"title": "Some movie title",
	"release_date": "2022-02-26"
}'
```

Delete a movie
```
curl --request DELETE \
  --url http://localhost:8090/rabbitmq-producer/movies/26
```

Healthcheck on the producer
```
curl --request GET \
  --url http://localhost:8090/rabbitmq-producer/actuator/health
```

Healthcheck on the consumer
```
curl --request GET \
  --url http://localhost:8091/actuator/health
```

An interface to manage RabbitMQ available on your browser at [this url](http://localhost:15672/rabbitmq/).

In this example, both the user and password are set to `rabbitmq`.
